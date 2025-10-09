package com.usersapi.web;

import com.usersapi.model.Gender;
import com.usersapi.service.UserService;
import com.usersapi.web.dto.CreateUserRequest;
import com.usersapi.web.dto.UpdatePhoneRequest;
import com.usersapi.web.dto.UpdateUserRequest;
import com.usersapi.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Создать пользователя", description = "Создание нового пользователя с опциональным телефоном")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь создан"),
            @ApiResponse(responseCode = "409", description = "Email уже существует",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "CONFLICT",
                            "message": "Email already exists: ivan.petrov@example.com"
                        }
                    """))),
            @ApiResponse(responseCode = "431", description = "Слишком длинные данные",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "REQUEST_HEADER_FIELDS_TOO_LARGE",
                            "message": "First name too long"
                        }
                    """)))
    })
    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userService.create(req);
    }

    @Operation(summary = "Получить пользователя", description = "Получение пользователя по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Integer id) {
        return userService.get(id);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаление пользователя по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "423", description = "Пользователь заблокирован",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "LOCKED",
                            "message": "User account is locked and cannot be deleted"
                        }
                    """)))
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }

    @Operation(summary = "Обновить пользователя", description = "Обновление данных пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "409", description = "Email уже существует",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "CONFLICT",
                            "message": "Email already exists: new.email@example.com"
                        }
                    """))),
            @ApiResponse(responseCode = "422", description = "Невалидные данные",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "UNPROCESSABLE_ENTITY",
                            "message": "Invalid first name format"
                        }
                    """))),
            @ApiResponse(responseCode = "423", description = "Пользователь заблокирован",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "LOCKED",
                            "message": "User is currently being modified by another request"
                        }
                    """)))
    })
    @PostMapping("/{id}")
    public UserResponse update(@PathVariable Integer id,
                               @Valid @RequestBody UpdateUserRequest req) {
        return userService.update(id, req);
    }

    @Operation(summary = "Обновить телефон", description = "Обновление телефона пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Телефон обновлен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "406", description = "Неприемлемый формат номера",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_ACCEPTABLE",
                            "message": "Phone number format not acceptable"
                        }
                    """)))
    })
    @PostMapping("/{id}/phone")
    public UserResponse updatePhone(@PathVariable Integer id,
                                    @Valid @RequestBody UpdatePhoneRequest req) {
        return userService.updatePhone(id, req);
    }

    @Operation(summary = "Загрузить аватар", description = "Загрузка аватара для пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Аватар загружен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """))),
            @ApiResponse(responseCode = "413", description = "Файл слишком большой",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "PAYLOAD_TOO_LARGE",
                            "message": "File size exceeds maximum allowed"
                        }
                    """))),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "UNSUPPORTED_MEDIA_TYPE",
                            "message": "Only image files are allowed"
                        }
                    """))),
            @ApiResponse(responseCode = "422", description = "Пустой файл",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "UNPROCESSABLE_ENTITY",
                            "message": "File is empty"
                        }
                    """)))
    })
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @Parameter(description = "ID пользователя") @PathVariable Integer id,
            @Parameter(description = "Файл изображения (макс. 5MB)")
            @RequestParam("file") MultipartFile file) {
        userService.uploadAvatar(id, file);
        return ResponseEntity.ok().body(new MessageResponse("Avatar uploaded successfully"));
    }

    @Operation(summary = "Получить аватар", description = "Получение аватара пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Аватар найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь или аватар не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "Avatar not found for user: 1"
                        }
                    """)))
    })
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Integer id) {
        byte[] avatar = userService.getAvatar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(avatar);
    }

    @Operation(summary = "Заблокировать пользователя", description = "Блокировка пользователя для изменений")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь заблокирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @PostMapping("/{id}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Integer id) {
        userService.lockUser(id);
        return ResponseEntity.ok().body(new MessageResponse("User locked successfully"));
    }

    @Operation(summary = "Разблокировать пользователя", description = "Разблокировка пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь разблокирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @PostMapping("/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Integer id) {
        userService.unlockUser(id);
        return ResponseEntity.ok().body(new MessageResponse("User unlocked successfully"));
    }

    @Operation(summary = "Проверить статус блокировки", description = "Проверка заблокирован ли пользователь")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "NOT_FOUND",
                            "message": "User not found: 9999"
                        }
                    """)))
    })
    @GetMapping("/{id}/lock-status")
    public ResponseEntity<?> getLockStatus(@PathVariable Integer id) {
        boolean isLocked = userService.isUserLocked(id);
        return ResponseEntity.ok().body(new LockStatusResponse(isLocked));
    }

    @Operation(summary = "Симулировать ошибку", description = "Специальный эндпоинт для тестирования внутренних ошибок")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Симулированная внутренняя ошибка",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "INTERNAL_ERROR",
                            "message": "An internal error occurred"
                        }
                    """)))
    })
    @PostMapping("/simulate-error")
    public ResponseEntity<?> simulateError() {
        userService.simulateInternalError();
        return ResponseEntity.ok().body(new MessageResponse("This should not be reached"));
    }

    @Operation(summary = "Список пользователей", description = "Получение списка пользователей с фильтрацией и пагинацией")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список получен"),
            @ApiResponse(responseCode = "405", description = "Метод не разрешен",
                    content = @Content(examples = @ExampleObject(value = """
                        {
                            "code": "METHOD_NOT_ALLOWED",
                            "message": "Request method 'PUT' not supported"
                        }
                    """)))
    })
    @GetMapping
    public Page<UserResponse> list(
            @Parameter(description = "Фильтр по имени") @RequestParam(required = false) String firstName,
            @Parameter(description = "Фильтр по фамилии") @RequestParam(required = false) String lastName,
            @Parameter(description = "Фильтр по email") @RequestParam(required = false) String email,
            @Parameter(description = "Фильтр по полу") @RequestParam(required = false) Gender gender,
            @Parameter(description = "Фильтр по бренду телефона") @RequestParam(required = false) String phoneBrand,
            @Parameter(description = "Фильтр по номеру телефона") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "Пагинация и сортировка") Pageable pageable
    ) {
        return userService.list(firstName, lastName, email, gender, phoneBrand, phoneNumber, pageable);
    }

    // Вспомогательные классы для ответов
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) {
            this.message = message;
        }
        public String getMessage() { return message; }
    }

    public static class LockStatusResponse {
        private boolean locked;
        public LockStatusResponse(boolean locked) {
            this.locked = locked;
        }
        public boolean isLocked() { return locked; }
    }
}