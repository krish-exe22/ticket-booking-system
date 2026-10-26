package com.pcp.moviebooking.repository;

import com.pcp.moviebooking.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);
}
