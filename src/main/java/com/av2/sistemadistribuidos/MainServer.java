package com.av2.sistemadistribuidos;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Classe principal do servidor
 * - Inicializa o servidor de nomes
 * - Configura o logger para registro de erros
 * - Gerencia o ciclo de vida do servidor
 */

public class MainServer {
    private static final LogManager logManager = LogManager.getInstance();
    private static NomeServidor servidor;
    private static ExecutorService executorService;

    public static void main(String[] args) {
        ConfigManager config = ConfigManager.getInstance();
        LogManager logManager = LogManager.getInstance();

        logManager.info("Iniciando servidor DNS");

        executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());
        servidor = new NomeServidor();
        logManager.info("Servidor configurado na porta " + config.getServerPort() +
                " com pool de " + config.getThreadPoolSize() + " threads");

        try {
            servidor.iniciar(executorService);
        } catch (Exception e) {
            logManager.severe("Erro ao iniciar servidor: " + e.getMessage());
        }


        // Registra shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logManager.info("Iniciando processo de shutdown do servidor");
            encerrarServidor();
        }));

    }

    private static void encerrarServidor() {
        try {
            logManager.info("Parando servidor");
            servidor.parar();

            logManager.info("Encerrando pool de threads");
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                logManager.warning("Timeout ao aguardar término das threads");
                executorService.shutdownNow();
            }

            logManager.info("Servidor encerrado com sucesso");
        } catch (InterruptedException e) {
            logManager.severe("Erro durante encerramento do servidor: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}



