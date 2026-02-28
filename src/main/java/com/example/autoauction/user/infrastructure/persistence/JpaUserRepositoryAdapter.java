package com.example.autoauction.user.infrastructure.persistence;

import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository jpaRepository;

    public JpaUserRepositoryAdapter(SpringDataUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        // Minimal adapter for now: roles persistence will be added with real use-cases.
        JpaUserEntity entity = new JpaUserEntity(
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive()
        );
        JpaUserEntity saved = jpaRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(UserMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(UserMapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

