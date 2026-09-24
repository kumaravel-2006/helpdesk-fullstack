package com.helpdesk.controller;

import com.helpdesk.entity.Role;
import com.helpdesk.entity.User;
import com.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/workers")
    public List<User> listWorkers(@RequestParam(required = false) String specialization) {
        List<User> workers = specialization != null
                ? userRepository.findByRoleAndSpecialization(Role.WORKER, specialization)
                : userRepository.findByRole(Role.WORKER);
        // Never leak password hashes to the frontend.
        workers.forEach(w -> w.setPasswordHash(null));
        return workers;
    }
}
