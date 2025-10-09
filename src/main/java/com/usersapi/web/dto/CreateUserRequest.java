package com.usersapi.web.dto;

import com.usersapi.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на создание пользователя")
public class CreateUserRequest {

    @NotBlank
    @Schema(description = "Имя пользователя", example = "Ivan", maxLength = 20)
    private String firstName;

    @NotBlank
    @Schema(description = "Фамилия пользователя", example = "Petrov")
    private String lastName;

    @Email
    @NotBlank
    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com")
    private String email;

    @NotNull
    @Schema(description = "Пол пользователя")
    private Gender gender;

    @Schema(description = "Телефон пользователя")
    private PhoneDto phone;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public PhoneDto getPhone() { return phone; }
    public void setPhone(PhoneDto phone) { this.phone = phone; }
}

