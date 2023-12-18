package com.ukonnra.wonderland.springelectrontest.desktop;

import com.ukonnra.wonderland.springelectrontest.entity.User;
import com.ukonnra.wonderland.springelectrontest.repository.UserRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<User> getUsers() {
    return this.userRepository.findAll();
  }
}
