package com.usersapi.web.dto;

import com.usersapi.model.Gender;

public class UserResponse {
    private Integer id;
    private Integer version;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private PhoneResponse phone;
    private String avatarFileName;
    private Boolean hasAvatar;
    private Boolean locked; // Новое поле для статуса блокировки

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public PhoneResponse getPhone() { return phone; }
    public void setPhone(PhoneResponse phone) { this.phone = phone; }

    public String getAvatarFileName() { return avatarFileName; }
    public void setAvatarFileName(String avatarFileName) { this.avatarFileName = avatarFileName; }

    public Boolean getHasAvatar() { return hasAvatar; }
    public void setHasAvatar(Boolean hasAvatar) { this.hasAvatar = hasAvatar; }

    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
}