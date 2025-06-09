package com.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.models.ModelosBases.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
