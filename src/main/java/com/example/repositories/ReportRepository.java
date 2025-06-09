package com.example.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.models.ModelosBases.Report;

import jakarta.transaction.Transactional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query(value = """
            SELECT r.*
            FROM tc_reports r
            JOIN tc_user_report ur ON r.id = ur.reportid
            WHERE ur.userid = :userId
            """, nativeQuery = true)
    List<Report> findReportsByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT COUNT(*)
            FROM tc_user_report
            WHERE userid = :userId AND reportid = :reportId
            """, nativeQuery = true)
    int existsByUserIdAndReportId(@Param("userId") Long userId, @Param("reportId") Long reportId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tc_user_report WHERE reportid = :reportId", nativeQuery = true)
    void deleteUserLink(@Param("reportId") Long reportId);

    @Modifying
    @Transactional
    @Query(value ="DELETE FROM tc_device_report WHERE reportid = :reportId", nativeQuery = true)
    void deleteDeviceLinks(@Param("reportId") Long reportId);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tc_user_report (userid, reportid) VALUES (:userId, :reportId)", nativeQuery = true)
    void linkUserToReport(@Param("userId") Long userId, @Param("reportId") Long reportId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tc_device_report (deviceid, reportid) VALUES (:deviceId, :reportId)", nativeQuery = true)
    void linkDeviceToReport(@Param("deviceId") Long deviceId, @Param("reportId") Long reportId);
}