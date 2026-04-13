package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);
    Optional<AppUser> findByMobile(String mobile);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMobile(String mobile);

    boolean existsByStudent_Id(Long studentId);
    Optional<AppUser> findByStudent_Id(Long studentId);

}
