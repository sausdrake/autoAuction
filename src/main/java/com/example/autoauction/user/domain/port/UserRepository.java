package com.example.autoauction.user.domain.port;

import com.example.autoauction.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    void deleteById(Long id);
}

