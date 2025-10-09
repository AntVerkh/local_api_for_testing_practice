package com.usersapi.config;

import com.usersapi.model.Gender;
import com.usersapi.model.Phone;
import com.usersapi.model.User;
import com.usersapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner seed(UserRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                var firstUser = new User();
                firstUser.setFirstName("Ivan");
                firstUser.setLastName("Petrov");
                firstUser.setEmail("ivan.petrov@example.com");
                firstUser.setGender(Gender.MALE);
                var phone = new Phone();
                phone.setNumber("+7-999-111-22-33");
                phone.setBrand("Samsung");
                firstUser.setPhone(phone);

                var secondUser = new User();
                secondUser.setFirstName("Anna");
                secondUser.setLastName("Ivanova");
                secondUser.setEmail("anna.ivanova@example.com");
                secondUser.setGender(Gender.FEMALE);

                repo.save(firstUser);
                repo.save(secondUser);
            }
        };
    }
}