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
    private static final LogManager logManager = LogManager.getInstance(12345);
    private static ExecutorService executorService;

    public static void main(String[] args) {
        ConfigManager config = ConfigManager.getInstance(12345);
        LogManager logManager = LogManager.getInstance(12345);

        logManager.info("Iniciando servidor DNS");
        executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());

        // Verifica se é servidor secundário pelos argumentos
        if (args.length > 0 && args[0].equals("secundario")) {
            try {
                int porta = Integer.parseInt(args[1]);
                ServidorReplicado servidorSecundario = new ServidorReplicado(false, porta);
                iniciarServidor(servidorSecundario, porta);
            } catch (NumberFormatException e) {
                logManager.severe("Porta inválida: " + args[1]);

            }
        } else {
            int portaPrimaria = config.getServerPort();
            ServidorReplicado servidorPrimario = new ServidorReplicado(true, portaPrimaria);
            servidorPrimario.adicionarServidorSecundario("localhost", 12346);
            servidorPrimario.adicionarServidorSecundario("localhost", 12347);
            iniciarServidor(servidorPrimario, portaPrimaria);
        }

    }

    private static void iniciarServidor(NomeServidor servidor, int porta) {
        try {
            logManager.info("Iniciando servidor na porta " + porta);
            servidor.iniciar(executorService);
        } catch (Exception e) {
            logManager.severe("Erro ao iniciar servidor: " + e.getMessage());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logManager.info("Iniciando processo de shutdown do servidor");
            encerrarServidor(servidor);
        }));
    }

    private static void encerrarServidor(NomeServidor servidor) {
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


