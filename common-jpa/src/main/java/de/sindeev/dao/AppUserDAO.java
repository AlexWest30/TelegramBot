package de.sindeev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import de.sindeev.entity.AppUser;

import java.util.Optional;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByTelegramUserId(Long id);
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByEmail(String email);
}
