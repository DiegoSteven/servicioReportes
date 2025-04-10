package com.example.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.models.ModelosBases.Device;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
        @Query("""
                SELECT d FROM Device d
                JOIN UserDevice ud ON d.id = ud.deviceId
                WHERE ud.userId = :userId
                AND (:deviceIds IS NULL OR d.id IN :deviceIds)
                AND (:groupIds IS NULL OR d.groupId IN :groupIds)
                """)
            List<Device> findAccessibleByUserId(
                @Param("userId") Long userId,
                @Param("deviceIds") Collection<Long> deviceIds,
                @Param("groupIds") Collection<Long> groupIds);
            
            
}