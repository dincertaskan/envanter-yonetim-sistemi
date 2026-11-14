package com.inventory.service;

import com.inventory.dto.AuthDtos.UserResponse;
import com.inventory.exception.BusinessException;
import com.inventory.exception.NotFoundException;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long id) {
        return UserResponse.from(getById(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAllByOrderByFirstNameAscLastNameAsc().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse changeRole(Long userId, Role newRole, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new BusinessException("Kendi rolünüzü değiştiremezsiniz");
        }
        User user = getById(userId);
        user.setRole(newRole);
        return UserResponse.from(user);
    }
}
