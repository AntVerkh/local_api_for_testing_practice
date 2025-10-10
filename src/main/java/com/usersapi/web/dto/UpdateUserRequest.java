package com.usersapi.web.dto;

import com.usersapi.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление пользователя")
public class UpdateUserRequest {

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "Имя пользователя", example = "Ivan")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Фамилия пользователя", example = "Petrov")
    private String lastName;

    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com")
    private String email;

    @Schema(description = "Пол пользователя")
    private Gender gender;

    // Геттеры и сеттеры
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
}