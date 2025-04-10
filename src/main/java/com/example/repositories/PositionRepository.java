package com.example.repositories;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.models.ModelosBases.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByDeviceIdAndFixTimeBetween(Long deviceId, Date from, Date to);

    @Query("SELECT p FROM Position p WHERE p.id IN :ids")
    List<Position> findByIds(@Param("ids") Collection<Long> ids);

}