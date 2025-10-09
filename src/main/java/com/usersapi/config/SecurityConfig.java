package com.usersapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable())) // для H2-консоли
                .authorizeHttpRequests(auth -> auth
                        // Swagger и H2 без авторизации
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**").permitAll()
                        // Разрешить загрузку файлов
                        .requestMatchers(HttpMethod.POST, "/api/users/{id}/avatar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}/avatar").permitAll()
                        // Peasant может только GET пользователей
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "PEASANT")
                        // Все остальные операции по /api/** только для ADMIN
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Basic Auth
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        var admin = User.withUsername("admin")
                .password(encoder.encode("admin"))
                .roles("ADMIN")
                .build();

        var peasant = User.withUsername("peasant")
                .password(encoder.encode("peasant"))
                .roles("PEASANT")
                .build();

        return new InMemoryUserDetailsManager(admin, peasant);
    }
}