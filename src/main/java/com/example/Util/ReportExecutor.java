package com.example.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class ReportExecutor {

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        // Un solo hilo para procesar reportes secuencialmente (puedes cambiarlo si necesitas concurrencia)
        executorService = Executors.newSingleThreadExecutor();
    }

    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}