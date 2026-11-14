package com.inventory.dto;

import com.inventory.model.Role;
import com.inventory.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Kimlik doğrulama ve kullanıcı işlemlerinde kullanılan istek / cevap modelleri.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "Email zorunludur")
            @Email(message = "Geçerli bir email giriniz")
            String email,

            @NotBlank(message = "Şifre zorunludur")
            String password
    ) {
    }

    public record RegisterRequest(
            @NotBlank(message = "Ad zorunludur")
            @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
            @Pattern(regexp = "^[\\p{L} ]+$", message = "Ad sadece harf içerebilir")
            String firstName,

            @NotBlank(message = "Soyad zorunludur")
            @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
            @Pattern(regexp = "^[\\p{L} ]+$", message = "Soyad sadece harf içerebilir")
            String lastName,

            @NotBlank(message = "Email zorunludur")
            @Email(message = "Geçerli bir email giriniz")
            @Size(max = 100)
            String email,

            @NotBlank(message = "Telefon zorunludur")
            @Pattern(regexp = "^[0-9]{10,15}$", message = "Telefon 10-15 haneli olmalıdır")
            String phone,

            @NotBlank(message = "Şifre zorunludur")
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$",
                    message = "Şifre en az 8 karakter olmalı; büyük harf, küçük harf ve özel karakter içermelidir"
            )
            String password
    ) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }

    public record UserResponse(
            Long id,
            String firstName,
            String lastName,
            String fullName,
            String email,
            String phone,
            Role role
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole()
            );
        }
    }

    public record RoleUpdateRequest(
            @NotNull(message = "Rol zorunludur")
            Role role
    ) {
    }
}
