package com.example.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.models.ModelosBases.Report;
import com.example.repositories.ReportRepository;

import jakarta.transaction.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> getReportsByUser(Long userId) {
        return reportRepository.findReportsByUserId(userId);
    }

    @Transactional
    public Report createReport(Report report, Long userId, List<Long> deviceIds) {
        Report savedReport = reportRepository.save(report);
    
        reportRepository.linkUserToReport(userId, savedReport.getId());
    
        if (deviceIds != null && !deviceIds.isEmpty()) {
            for (Long deviceId : deviceIds) {
                reportRepository.linkDeviceToReport(deviceId, savedReport.getId());
            }
        }
    
        return savedReport;
    }
    

    @Transactional
    public boolean deleteReportIfOwnedByUser(Long userId, Long reportId) {
        if (reportRepository.existsByUserIdAndReportId(userId, reportId) > 0) {
            reportRepository.deleteDeviceLinks(reportId);   // ⬅️ Elimina de tc_device_report
            reportRepository.deleteUserLink(reportId);      // ⬅️ Elimina de tc_user_report
            reportRepository.deleteById(reportId);          // ⬅️ Elimina de tc_reports
            return true;
        }
        return false;
    }
    
}
