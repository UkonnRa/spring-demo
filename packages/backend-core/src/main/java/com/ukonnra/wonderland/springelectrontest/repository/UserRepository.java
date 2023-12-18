package com.ukonnra.wonderland.springelectrontest.repository;

import com.ukonnra.wonderland.springelectrontest.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {}
