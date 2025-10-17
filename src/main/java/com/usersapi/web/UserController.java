package com.usersapi.web;

import com.usersapi.model.Gender;
import com.usersapi.service.UserService;
import com.usersapi.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Users API v1 - User management operations")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get users with pagination", description = "Retrieve paginated list of users with filtering and sorting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> list(
            @Parameter(description = "Filter by first name") @RequestParam(required = false) String firstName,
            @Parameter(description = "Filter by last name") @RequestParam(required = false) String lastName,
            @Parameter(description = "Filter by email") @RequestParam(required = false) String email,
            @Parameter(description = "Filter by gender") @RequestParam(required = false) Gender gender,
            @Parameter(description = "Filter by phone brand") @RequestParam(required = false) String phoneBrand,
            @Parameter(description = "Filter by phone number") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field (format: field,asc|desc). Multiple sorts supported: sort=firstName,asc&sort=lastName,desc")
            @RequestParam(required = false) List<String> sort) {

        logger.info("Processing users list request - page: {}, size: {}, sort: {}", page, size, sort);

        // Create pageable with sorting
        Pageable pageable = createPageable(page, size, sort);

        Page<UserResponse> usersPage = userService.list(firstName, lastName, email, gender, phoneBrand, phoneNumber, pageable);

        PageResponse<UserResponse> response = new PageResponse<>(usersPage);

        logger.info("Returning {} users on page {} of {}",
                response.getContent().size(), page, response.getMetadata().getTotalPages());

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(response);
    }

    @Operation(summary = "Create user", description = "Create a new user. Required fields: firstName, lastName, email, gender")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid data",
                    content = @Content(examples = {
                            @ExampleObject(name = "Missing required field", value = """
                            {
                                "code": "VALIDATION_ERROR",
                                "message": "Validation failed",
                                "details": {
                                    "firstName": "First name is required",
                                    "lastName": "Last name is required"
                                }
                            }
                        """),
                            @ExampleObject(name = "Invalid email", value = """
                            {
                                "code": "VALIDATION_ERROR",
                                "message": "Validation failed",
                                "details": {
                                    "email": "Email should be valid"
                                }
                            }
                        """),
                            @ExampleObject(name = "Field too long", value = """
                            {
                                "code": "VALIDATION_ERROR",
                                "message": "Validation failed",
                                "details": {
                                    "firstName": "First name must be between 1 and 100 characters"
                                }
                            }
                        """)
                    })),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "CONFLICT",
                            "message": "Email already exists: ivan.petrov@example.com"
                        }
                    """)))
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());

        UserResponse response = userService.create(request);

        logger.info("User created successfully with ID: {}", response.getId());

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(response);
    }

    @Operation(summary = "Get user", description = "Get user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Integer id) {
        logger.debug("Fetching user with ID: {}", id);

        UserResponse response = userService.get(id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(response);
    }

    @Operation(summary = "Delete user", description = "Delete user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "423", description = "User locked",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "LOCKED",
                            "message": "User account is locked and cannot be deleted"
                        }
                    """)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Integer id) {
        logger.info("Deleting user with ID: {}", id);

        userService.delete(id);

        logger.info("User deleted successfully with ID: {}", id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(new MessageResponse("User deleted successfully"));
    }

    @Operation(summary = "Update user", description = "Update user data. All fields are optional")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "VALIDATION_ERROR",
                            "message": "Validation failed",
                            "details": {
                                "email": "Email should be valid",
                                "firstName": "First name must be between 1 and 100 characters"
                            }
                        }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "CONFLICT",
                            "message": "Email already exists: new.email@example.com"
                        }
                    """))),
            @ApiResponse(responseCode = "422", description = "Invalid data",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "UNPROCESSABLE_ENTITY",
                            "message": "Invalid first name format"
                        }
                    """))),
            @ApiResponse(responseCode = "423", description = "User locked",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "LOCKED",
                            "message": "User is currently being modified by another request"
                        }
                    """)))
    })
    @PostMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Integer id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", id);

        UserResponse response = userService.update(id, request);

        logger.info("User updated successfully with ID: {}", id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(response);
    }

    @Operation(summary = "Update phone", description = "Update user's phone. All fields are optional")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Phone updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "VALIDATION_ERROR",
                            "message": "Validation failed",
                            "details": {
                                "number": "Phone number can only contain digits, +, -, (, ) and spaces"
                            }
                        }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "406", description = "Phone number format not acceptable",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_ACCEPTABLE",
                            "message": "Phone number format not acceptable"
                        }
                    """)))
    })
    @PostMapping("/{id}/phone")
    public ResponseEntity<UserResponse> updatePhone(@PathVariable Integer id,
                                                    @Valid @RequestBody UpdatePhoneRequest request) {
        logger.info("Updating phone for user ID: {}", id);

        UserResponse response = userService.updatePhone(id, request);

        logger.info("Phone updated successfully for user ID: {}", id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(response);
    }

    @Operation(summary = "Upload avatar", description = "Upload avatar for user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avatar uploaded"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "413", description = "File too large",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "PAYLOAD_TOO_LARGE",
                            "message": "File size exceeds maximum allowed"
                        }
                    """))),
            @ApiResponse(responseCode = "415", description = "Unsupported file type",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "UNSUPPORTED_MEDIA_TYPE",
                            "message": "Only image files are allowed"
                        }
                    """))),
            @ApiResponse(responseCode = "422", description = "Empty file",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "UNPROCESSABLE_ENTITY",
                            "message": "File is empty"
                        }
                    """)))
    })
    @PostMapping("/{id}/avatar")
    public ResponseEntity<MessageResponse> uploadAvatar(
            @Parameter(description = "User ID") @PathVariable Integer id,
            @Parameter(description = "Image file (max 5MB)")
            @RequestParam("file") MultipartFile file) {
        logger.info("Uploading avatar for user ID: {}", id);

        userService.uploadAvatar(id, file);

        logger.info("Avatar uploaded successfully for user ID: {}", id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(new MessageResponse("Avatar uploaded successfully"));
    }

    @Operation(summary = "Get avatar", description = "Get user's avatar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avatar found"),
            @ApiResponse(responseCode = "404", description = "User or avatar not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "Avatar not found for user: 1"
                        }
                    """)))
    })
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Integer id) {
        logger.debug("Fetching avatar for user ID: {}", id);

        byte[] avatar = userService.getAvatar(id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .contentType(MediaType.IMAGE_JPEG)
                .body(avatar);
    }

    @Operation(summary = "Lock user", description = "Lock user for modifications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User locked"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @PostMapping("/{id}/lock")
    public ResponseEntity<MessageResponse> lockUser(@PathVariable Integer id) {
        logger.info("Locking user with ID: {}", id);

        userService.lockUser(id);

        logger.info("User locked successfully with ID: {}", id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(new MessageResponse("User locked successfully"));
    }

    @Operation(summary = "Unlock user", description = "Unlock user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User unlocked"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @PostMapping("/{id}/unlock")
    public ResponseEntity<MessageResponse> unlockUser(@PathVariable Integer id) {
        logger.info("Unlocking user with ID: {}", id);

        userService.unlockUser(id);

        logger.info("User unlocked successfully with ID: {}", id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(new MessageResponse("User unlocked successfully"));
    }

    @Operation(summary = "Check lock status", description = "Check if user is locked")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lock status retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @GetMapping("/{id}/lock-status")
    public ResponseEntity<LockStatusResponse> getLockStatus(@PathVariable Integer id) {
        logger.debug("Checking lock status for user ID: {}", id);

        boolean isLocked = userService.isUserLocked(id);

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(new LockStatusResponse(isLocked));
    }

    @Operation(summary = "Simulate error", description = "Simulate internal server error for testing")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Simulated internal error",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "INTERNAL_ERROR",
                            "message": "An internal error occurred"
                        }
                    """)))
    })
    @PostMapping("/simulate-error")
    public ResponseEntity<MessageResponse> simulateError() {
        logger.warn("Simulating internal server error");

        userService.simulateInternalError();

        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(new MessageResponse("This should not be reached"));
    }

    private Pageable createPageable(int page, int size, List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            // Default sort by ID if no sort specified
            return PageRequest.of(page, size, Sort.by("id").ascending());
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sort) {
            String[] sortParts = sortParam.split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                String direction = sortParts[1].trim().toLowerCase();

                // Validate field name to prevent injection
                if (isValidSortField(field)) {
                    Sort.Order order = "desc".equals(direction) ?
                            Sort.Order.desc(field) : Sort.Order.asc(field);
                    orders.add(order);
                } else {
                    logger.warn("Invalid sort field requested: {}", field);
                }
            } else {
                logger.warn("Invalid sort parameter format: {}", sortParam);
            }
        }

        if (orders.isEmpty()) {
            return PageRequest.of(page, size, Sort.by("id").ascending());
        }

        return PageRequest.of(page, size, Sort.by(orders));
    }

    private boolean isValidSortField(String field) {
        // Whitelist of allowed sort fields
        return List.of("id", "firstName", "lastName", "email", "gender").contains(field);
    }

    // Response DTO classes
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) {
            this.message = message;
        }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class LockStatusResponse {
        private boolean locked;
        public LockStatusResponse(boolean locked) {
            this.locked = locked;
        }
        public boolean isLocked() { return locked; }
        public void setLocked(boolean locked) { this.locked = locked; }
    }
}