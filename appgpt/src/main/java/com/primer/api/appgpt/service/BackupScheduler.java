package com.primer.api.appgpt.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    @Scheduled(cron = "0 0 0 * * *")
    public void backupDiario() {
        try {
            String fecha = LocalDateTime.now(java.time.ZoneId.of("America/Lima"))
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            File dir = new File("backups");
            if (!dir.exists()) dir.mkdirs();
            File salida = new File(dir, "sabores_" + fecha + ".sql");
            String dbUser = System.getenv().getOrDefault("DB_USERNAME", "root");
            String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "");
            String dbName = System.getenv().getOrDefault("DB_NAME", "SaboresAutenticos");
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump", "-u", dbUser, "-p" + dbPassword, dbName);
            pb.redirectOutput(salida);
            Process p = pb.start();
            int code = p.waitFor();
            if (code == 0) {
                log.info("Backup automático generado: {}", salida.getAbsolutePath());
            } else {
                log.warn("Backup automático falló con código {}", code);
            }
        } catch (Exception ex) {
            log.warn("No se pudo ejecutar el backup automático: {}", ex.getMessage());
        }
    }
}