package com.av2.sistemadistribuidos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LogManager {
    private static LogManager instance;
    private final Logger logger;

    private LogManager() {
        this.logger = Logger.getLogger("ServidorLogger");
        configurarLogger();
    }

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    private void configurarLogger() {
        try {
            String logDirectory = "logs"; // Convertido para variável local
            // Criar diretório de logs
            Path logsDir = Paths.get(logDirectory);

            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }

            // Nome do arquivo de log
            String nomeArquivo = String.format("%s/servidor_%s.txt",
                    logDirectory,
                    new SimpleDateFormat("yyyyMMdd").format(new Date()));

            FileHandler fileHandler = criarFileHandler(nomeArquivo);
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);

            info("Sistema de logs inicializado com sucesso");

        } catch (IOException e) {
            System.err.println("Erro ao configurar sistema de logs: " + e.getMessage());
        }
    }

    private FileHandler criarFileHandler(String nomeArquivo) throws IOException {
        FileHandler fileHandler = new FileHandler(nomeArquivo, true);
        fileHandler.setFormatter(criarFormatador());
        return fileHandler;
    }

    private SimpleFormatter criarFormatador() {
        return new SimpleFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public synchronized String format(LogRecord record) {
                return String.format("[%s] [%s] %s%n",
                        dateFormat.format(new Date(record.getMillis())),
                        record.getLevel().getName(),
                        record.getMessage());
            }
        };
    }


    // Métodos para logging
    public void info(String mensagem) {
        logger.info(mensagem);
    }

    public void warning(String mensagem) {
        logger.warning(mensagem);
    }

    public void severe(String mensagem) {
        logger.severe(mensagem);
    }

    public void fine(String mensagem) {
        logger.fine(mensagem);
    }
}