package com.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.models.ModelosBases.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}