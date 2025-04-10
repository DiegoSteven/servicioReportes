package com.example.services;

import java.io.IOException;
import java.util.Collection;

import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class CsvExportService {

    private final ReportUtils reportUtils;

    public CsvExportService(ReportUtils reportUtils) {
        this.reportUtils = reportUtils;
    }

    public <T> void exportToCsv(
            Collection<T> data,
            HttpServletResponse response,
            String fileName) throws IOException {

        // Por ahora, dejamos solo la estructura. El contenido se completará al final.
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".csv\"");

        // Aquí se usará alguna librería como OpenCSV o simplemente PrintWriter más adelante
        response.getWriter().write("CSV export placeholder");
    }
}