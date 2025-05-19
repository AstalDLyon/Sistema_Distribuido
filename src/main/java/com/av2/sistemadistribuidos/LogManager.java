package com.av2.sistemadistribuidos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LogManager {
    private static final Map<Integer, LogManager> instances = new ConcurrentHashMap<>();
    private final Logger logger;
    private final int porta;

    private LogManager(int porta) {
        this.porta = porta;
        this.logger = Logger.getLogger("ServidorLogger_" + porta);
        configurarLogger();
    }
/* A implementação a baixo foi feita usando a porta padrão diretamente.
* Com intuito de evitar uma dependencia circular,
*  ja que o LogManager depende do ConfigManager para obter uma porta padrão
* e o config manager depende do log para registrar... logs.
* Implementando desse jeito eu evito a dependencia circular.
* É elegante? não, mas serve o proposito*/
    public static synchronized LogManager getInstance() {
        int portaPadrao = 12345; // Define a porta padrão diretamente
        return getInstance(portaPadrao);
    }

    public static synchronized LogManager getInstance(int porta) {
        return instances.computeIfAbsent(porta, LogManager::new);

    }



    private void configurarLogger() {// Configura sistema de logs
        // Organiza logs por data e porta do servidor
        // Implementa rotação de arquivos de log

        try {
            String logDirectory = "logs/servidor_" + porta;
            Path logsDir = Paths.get(logDirectory);

            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }

            String nomeArquivo = String.format("%s/servidor_%d_%s.txt",
                    logDirectory,
                    porta,
                    new SimpleDateFormat("yyyyMMdd").format(new Date()));

            FileHandler fileHandler = criarFileHandler(nomeArquivo);
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);

            info("Sistema de logs inicializado para servidor na porta " + porta);
        } catch (IOException e) {
            System.err.println("Erro ao configurar sistema de logs para porta " + porta + ": " + e.getMessage());
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
