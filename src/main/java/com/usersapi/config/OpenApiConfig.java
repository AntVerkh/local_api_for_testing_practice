package com.usersapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Users API",
                version = "v1",
                description = """
            CRUD пользователей с телефонами и аватарами.
            
            ## Примеры ошибок:
            
            ### 404 Not Found
            - Пользователь не найден: GET /api/users/9999
            - Аватар не найден: GET /api/users/1/avatar (если аватар не загружен)
            
            ### 405 Method Not Allowed
            - PUT /api/users/1 (используйте POST вместо PUT)
            - PATCH /api/users/1 (используйте POST вместо PATCH)
            
            ### 406 Not Acceptable
            - POST /api/users/1/phone с номером "abc" (только цифры и символы)
            
            ### 409 Conflict
            - POST /api/users с email, который уже существует
            
            ### 413 Payload Too Large
            - POST /api/users/1/avatar с файлом > 5MB
            
            ### 415 Unsupported Media Type
            - POST /api/users/1/avatar с файлом .txt вместо изображения
            
            ### 422 Unprocessable Entity
            - POST /api/users/1/avatar с пустым файлом
            - POST /api/users/1 с firstName = "invalid"
            
            ### 423 Locked
            - DELETE /api/users/1 когда пользователь заблокирован
            - POST /api/users/1 когда пользователь редактируется другим запросом
            
            ### 431 Request Header Fields Too Large
            - POST /api/users с firstName длиннее 20 символов
            
            ### 500 Internal Server Error
            - POST /api/users/simulate-error (специальный эндпоинт для тестирования)
            """
        ),
        security = { @SecurityRequirement(name = "basicAuth") }
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class OpenApiConfig {
}