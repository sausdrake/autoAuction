package com.example.autoauction.user.infrastructure.persistence;

import com.example.autoauction.user.domain.Role;
import com.example.autoauction.user.domain.port.RoleRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaRoleRepositoryAdapter implements RoleRepository {

    private final SpringDataRoleJpaRepository jpaRepository;

    public JpaRoleRepositoryAdapter(SpringDataRoleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Role save(Role role) {
        JpaRoleEntity entity = new JpaRoleEntity(role.getName(), role.getDescription());
        JpaRoleEntity saved = jpaRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return jpaRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name).map(UserMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return jpaRepository.findAll().stream().map(UserMapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

