package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIngnoreCase(String email);
    boolean existsByEmailIngnoreCase(String email);

}
