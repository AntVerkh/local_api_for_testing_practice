package com.usersapi.service;

import com.usersapi.model.Gender;
import com.usersapi.model.Phone;
import com.usersapi.model.User;
import com.usersapi.repository.UserRepository;
import com.usersapi.repository.UserSpecifications;
import com.usersapi.web.dto.*;
import com.usersapi.web.errors.Errors.*;
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
        // Проверка на слишком длинные заголовки (имитация 431)
        if (createUserRequest.getFirstName() != null && createUserRequest.getFirstName().length() > 8) {
            throw new HeaderTooLargeException("First name too long");
        }

        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new ConflictException("Email already exists: " + createUserRequest.getEmail());
        }

        User user = new User();
        user.setFirstName(createUserRequest.getFirstName());
        user.setLastName(createUserRequest.getLastName());
        user.setEmail(createUserRequest.getEmail());
        user.setGender(createUserRequest.getGender());

        if (createUserRequest.getPhone() != null) {
            Phone phone = new Phone();
            phone.setNumber(createUserRequest.getPhone().getNumber());
            phone.setBrand(createUserRequest.getPhone().getBrand());
            user.setPhone(phone);
        }

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public UserResponse get(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return toResponse(user);
    }

    public void delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }

        // Проверка на заблокированного пользователя (423)
        if (isUserLocked(id)) {
            throw new LockedException("User account is locked and cannot be deleted");
        }

        userRepository.deleteById(id);
    }

    public UserResponse update(Integer id, UpdateUserRequest req) {
        // Получаем блокировку для пользователя (423 в случае блокировки)
        ReentrantLock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new LockedException("User is currently being modified by another request");
        }

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found: " + id));

            // Проверка на необрабатываемые данные (422)
            if (req.getFirstName() != null && "invalid".equalsIgnoreCase(req.getFirstName())) {
                throw new UnprocessableEntityException("Invalid first name format");
            }

            if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
            if (req.getLastName() != null) user.setLastName(req.getLastName());
            if (req.getEmail() != null) {
                if (!user.getEmail().equals(req.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
                    throw new ConflictException("Email already exists: " + req.getEmail());
                }
                user.setEmail(req.getEmail());
            }
            if (req.getGender() != null) user.setGender(req.getGender());

            return toResponse(user);
        } finally {
            lock.unlock();
            userLocks.remove(id);
        }
    }

    public UserResponse updatePhone(Integer userId, UpdatePhoneRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        // Проверка на неприемлемый формат номера (406)
        if (req.getNumber() != null && !isAcceptablePhoneNumber(req.getNumber())) {
            throw new NotAcceptableException("Phone number format not acceptable");
        }

        Phone phone = user.getPhone();
        if (phone == null) {
            phone = new Phone();
            user.setPhone(phone);
        }
        if (req.getNumber() != null) phone.setNumber(req.getNumber());
        if (req.getBrand() != null) phone.setBrand(req.getBrand());

        return toResponse(user);
    }

    public void uploadAvatar(Integer userId, MultipartFile file) {
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
            }

            // Generate unique filename
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = avatarStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            // Update user entity
            user.setAvatarFileName(fileName);
            user.setAvatarFileSize(file.getSize());
            user.setAvatarContentType(contentType);

        } catch (IOException ex) {
            throw new InternalErrorException("Could not store file: " + ex.getMessage());
        }
    }

    public byte[] getAvatar(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getAvatarFileName() == null) {
            throw new NotFoundException("Avatar not found for user: " + userId);
        }

        try {
            Path filePath = avatarStorageLocation.resolve(user.getAvatarFileName()).normalize();
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new InternalErrorException("Could not read file: " + ex.getMessage());
        }
    }

    // Метод для принудительной блокировки пользователя (423)
    public void lockUser(Integer userId) {
        ReentrantLock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
    }

    // Метод для разблокировки пользователя
    public void unlockUser(Integer userId) {
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

        Specification<User> spec = where(UserSpecifications.firstNameContains(firstName))
                .and(UserSpecifications.lastNameContains(lastName))
                .and(UserSpecifications.emailContains(email))
                .and(UserSpecifications.genderEquals(gender))
                .and(UserSpecifications.phoneBrandContains(phoneBrand))
                .and(UserSpecifications.phoneNumberContains(phoneNumber));

        return userRepository.findAll(spec, pageable).map(this::toResponse);
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