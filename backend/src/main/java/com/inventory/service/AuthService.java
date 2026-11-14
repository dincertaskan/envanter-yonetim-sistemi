package com.inventory.service;

import com.inventory.dto.AuthDtos.AuthResponse;
import com.inventory.dto.AuthDtos.LoginRequest;
import com.inventory.dto.AuthDtos.RegisterRequest;
import com.inventory.dto.AuthDtos.UserResponse;
import com.inventory.exception.BusinessException;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import com.inventory.security.JwtService;
import com.inventory.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Yeni kullanıcı kaydı. Kayıt olan herkes USER rolüyle başlar;
     * yönetici yetkisini sadece başka bir yönetici verebilir.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Bu email adresi zaten kayıtlı", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException("Bu telefon numarası zaten kayıtlı", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        return toAuthResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Email veya şifre hatalı"));

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(UserPrincipal.from(user));
        return new AuthResponse(token, UserResponse.from(user));
    }
}
