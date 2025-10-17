package com.usersapi.service;

import com.usersapi.model.Gender;
import com.usersapi.model.Phone;
import com.usersapi.model.User;
import com.usersapi.repository.UserRepository;
import com.usersapi.repository.UserSpecifications;
import com.usersapi.web.dto.*;
import com.usersapi.web.errors.Errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final Path avatarStorageLocation;
    private final ConcurrentHashMap<Integer, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.avatarStorageLocation = Paths.get("uploads/avatars").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.avatarStorageLocation);
        } catch (IOException ex) {
            throw new InternalErrorException("Could not create upload directory");
        }
    }

    public UserResponse create(CreateUserRequest createUserRequest) {
        logger.info("Creating new user with email: {}", createUserRequest.getEmail());

        // Валидация email на уникальность
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new ConflictException("Email already exists: " + createUserRequest.getEmail());
        }

        User user = new User();
        user.setFirstName(createUserRequest.getFirstName().trim());
        user.setLastName(createUserRequest.getLastName().trim());
        user.setEmail(createUserRequest.getEmail().trim().toLowerCase());
        user.setGender(createUserRequest.getGender());

        if (createUserRequest.getPhone() != null) {
            Phone phone = new Phone();
            phone.setNumber(createUserRequest.getPhone().getNumber() != null ?
                    createUserRequest.getPhone().getNumber().trim() : null);
            phone.setBrand(createUserRequest.getPhone().getBrand() != null ?
                    createUserRequest.getPhone().getBrand().trim() : null);
            user.setPhone(phone);
        }

        User saved = userRepository.save(user);
        logger.info("User created successfully with ID: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Integer id) {
        logger.debug("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        return toResponse(user);
    }

    public void delete(Integer id) {
        logger.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }

        // Проверка на заблокированного пользователя (423)
        if (isUserLocked(id)) {
            throw new LockedException("User account is locked and cannot be deleted");
        }

        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }

    public UserResponse update(Integer id, UpdateUserRequest updateUserRequest) {
        logger.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (updateUserRequest.getFirstName() != null) {
            user.setFirstName(updateUserRequest.getFirstName().trim());
        }
        if (updateUserRequest.getLastName() != null) {
            user.setLastName(updateUserRequest.getLastName().trim());
        }
        if (updateUserRequest.getEmail() != null) {
            String newEmail = updateUserRequest.getEmail().trim().toLowerCase();
            if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
                throw new ConflictException("Email already exists: " + newEmail);
            }
            user.setEmail(newEmail);
        }
        if (updateUserRequest.getGender() != null) {
            user.setGender(updateUserRequest.getGender());
        }

        User updated = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", id);

        return toResponse(updated);
    }

    public UserResponse updatePhone(Integer userId, UpdatePhoneRequest updatePhoneRequest) {
        logger.info("Updating phone for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // Дополнительная бизнес-валидация номера телефона
        if (updatePhoneRequest.getNumber() != null && !isAcceptablePhoneNumber(updatePhoneRequest.getNumber())) {
            throw new NotAcceptableException("Phone number format not acceptable");
        }

        Phone phone = user.getPhone();
        if (phone == null) {
            phone = new Phone();
            user.setPhone(phone);
        }
        if (updatePhoneRequest.getNumber() != null) {
            phone.setNumber(updatePhoneRequest.getNumber().trim());
        }
        if (updatePhoneRequest.getBrand() != null) {
            phone.setBrand(updatePhoneRequest.getBrand().trim());
        }

        User updated = userRepository.save(user);
        logger.info("Phone updated successfully for user ID: {}", userId);

        return toResponse(updated);
    }

    public void uploadAvatar(Integer userId, MultipartFile file) {
        logger.info("Uploading avatar for user ID: {}", userId);

        if (file.isEmpty()) {
            throw new UnprocessableEntityException("File is empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new PayloadTooLargeException("File size exceeds limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UnsupportedMediaTypeException("Only image files are allowed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        try {
            // Delete old avatar if exists
            if (user.getAvatarFileName() != null) {
                Path oldFile = avatarStorageLocation.resolve(user.getAvatarFileName());
                Files.deleteIfExists(oldFile);
                logger.debug("Deleted old avatar for user ID: {}", userId);
            }

            // Generate unique filename
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = avatarStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            // Update user entity
            user.setAvatarFileName(fileName);
            user.setAvatarFileSize(file.getSize());
            user.setAvatarContentType(contentType);

            userRepository.save(user);
            logger.info("Avatar uploaded successfully for user ID: {}", userId);

        } catch (IOException ioException) {
            logger.error("Error uploading avatar for user ID: {}", userId, ioException);
            throw new InternalErrorException("Could not store file: " + ioException.getMessage());
        }
    }

    public byte[] getAvatar(Integer userId) {
        logger.debug("Fetching avatar for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getAvatarFileName() == null) {
            throw new NotFoundException("Avatar not found for user: " + userId);
        }

        try {
            Path filePath = avatarStorageLocation.resolve(user.getAvatarFileName()).normalize();
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            logger.error("Error reading avatar file for user ID: {}", userId, ex);
            throw new InternalErrorException("Could not read file: " + ex.getMessage());
        }
    }

    // Метод для принудительной блокировки пользователя (423)
    public void lockUser(Integer userId) {
        logger.info("Locking user with ID: {}", userId);

        ReentrantLock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
    }

    // Метод для разблокировки пользователя
    public void unlockUser(Integer userId) {
        logger.info("Unlocking user with ID: {}", userId);

        ReentrantLock lock = userLocks.get(userId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            userLocks.remove(userId);
        }
    }

    // Метод для проверки заблокирован ли пользователь
    public boolean isUserLocked(Integer userId) {
        ReentrantLock lock = userLocks.get(userId);
        return lock != null && lock.isLocked();
    }

    // Метод для симуляции внутренней ошибки (500)
    public void simulateInternalError() {
        logger.error("Simulating internal server error");
        throw new InternalErrorException("Simulated internal server error");
    }

    // Метод для проверки допустимости номера телефона
    private boolean isAcceptablePhoneNumber(String number) {
        // Простая валидация - номер должен содержать только цифры, +, -, пробелы и скобки
        return number != null && number.matches("^[\\d\\+\\-\\(\\)\\s]+$");
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(
            String firstName,
            String lastName,
            String email,
            Gender gender,
            String phoneBrand,
            String phoneNumber,
            Pageable pageable) {

        logger.debug("Fetching users list with filters - firstName: {}, lastName: {}, email: {}, gender: {}, phoneBrand: {}, phoneNumber: {}, page: {}, size: {}",
                firstName, lastName, email, gender, phoneBrand, phoneNumber, pageable.getPageNumber(), pageable.getPageSize());

        Specification<User> spec = where(UserSpecifications.firstNameContains(firstName))
                .and(UserSpecifications.lastNameContains(lastName))
                .and(UserSpecifications.emailContains(email))
                .and(UserSpecifications.genderEquals(gender))
                .and(UserSpecifications.phoneBrandContains(phoneBrand))
                .and(UserSpecifications.phoneNumberContains(phoneNumber));

        Page<UserResponse> result = userRepository.findAll(spec, pageable).map(this::toResponse);
        logger.debug("Found {} users on page {} of {}",
                result.getNumberOfElements(), result.getNumber(), result.getTotalPages());

        return result;
    }

    private UserResponse toResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setVersion(user.getVersion());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());
        userResponse.setGender(user.getGender());

        if (user.getPhone() != null) {
            PhoneResponse phoneResponse = new PhoneResponse();
            phoneResponse.setId(user.getPhone().getId());
            phoneResponse.setNumber(user.getPhone().getNumber());
            phoneResponse.setBrand(user.getPhone().getBrand());
            userResponse.setPhone(phoneResponse);
        }

        userResponse.setAvatarFileName(user.getAvatarFileName());
        userResponse.setHasAvatar(user.getAvatarFileName() != null);
        userResponse.setLocked(isUserLocked(user.getId()));

        return userResponse;
    }
}