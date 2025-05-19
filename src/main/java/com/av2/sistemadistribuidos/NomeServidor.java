package com.av2.sistemadistribuidos;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public class NomeServidor {
    private final Map<String, String> hostTable = new HashMap<>();
    private final FileManager fileManager;
    private final ConfigManager config;
    private final LogManager logManager;
    private ExecutorService executorService;
    private volatile boolean running;

    public NomeServidor(int porta) {
        this.logManager = LogManager.getInstance(porta);
        this.config = ConfigManager.getInstance(porta);
        this.fileManager = new FileManager(config.getFilePath(), porta);
        logManager.info(("NomeServidor inicializado na porta" + porta));
    }

    public void iniciar(ExecutorService executorService) {
        if (executorService == null) {
            throw new IllegalArgumentException("ExecutorService não pode ser nulo");
        }
        this.executorService = executorService;
        this.running = true;
        fileManager.carregarRegistros(hostTable);

        try (ServerSocket serverSocket = new ServerSocket(config.getServerPort())) {
            serverSocket.setSoTimeout(config.getServerTimeout());
            logManager.info("Servidor iniciado na porta " + config.getServerPort());

            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    executorService.submit(() -> handleClient(client));
                } catch (SocketTimeoutException e) {
                    logManager.fine("Timeout de accept() - continuando operação normal");
                } catch (IOException e) {
                    if (running) {
                        logManager.severe("Erro ao aceitar conexão: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logManager.severe("Erro fatal ao iniciar o servidor: " + e.getMessage());
        }
    }

    public void parar() {
        running = false;
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logManager.info("Servidor encerrado");
    }



    //  Registra novo par hostname→IP
    //  Verifica duplicatas
    //  Persiste registro em arquivo
    //  Thread-safe usando synchronized
    protected boolean registerHost(String hostname, String ip) {
        logManager.info(String.format("Tentativa de registro: %s -> %s", hostname, ip));
        if (hostname == null || hostname.trim().isEmpty()) {
            logManager.warning("Hostname inválido ou vazio");
            return false;
        }
        if (ip == null || ip.trim().isEmpty()) {
            logManager.warning("IP inválido ou vazio");
            return false;
        }
        // Normaliza os valores removendo espaços extras
        hostname = hostname.trim();
        ip = ip.trim();



        synchronized (hostTable) {
            try {
                // Verifica se já existe
                if (hostTable.containsKey(hostname)) {
                    logManager.warning("Hostname já registrado: " + hostname);
                    return false;
                }
                if (hostTable.containsValue(ip)) {
                    logManager.warning("IP já registrado: " + ip);
                    return false;
                }

                // Tenta salvar primeiro no arquivo
                fileManager.salvarRegistro(hostname, ip);

                // Se salvou com sucesso, atualiza a tabela em memória
                hostTable.put(hostname, ip);
                logManager.info("Registro bem-sucedido: " + hostname + " -> " + ip);
                return true;

            } catch (Exception e) {
                logManager.severe("Erro ao registrar host: " + e.getMessage());
                // Garante que não fique em estado inconsistente
                hostTable.remove(hostname);
                return false;
            }
        }

    }

    //Méto-do para desligar o servidor

    //handleClient(clientSocket):
    // - Processa requisições individuais
    //  Parse de comandos LOOKUP e REGISTER
    // Envia respostas ao cliente

    protected void handleClient(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        logManager.info("Processando cliente: " + clientAddress);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            logManager.fine("Requisição de " + clientAddress + ": " + request);

            if (request == null || request.trim().isEmpty()) {
                logManager.warning(String.format("Requisição vazia de %s", clientAddress));
                out.println("ERRO: Requisição vazia");
                return;
            }

            String[] parts = request.trim().split("\\s+");

            if (parts.length == 0) {
                out.println("ERRO: Comando vazio");
                return;
            }

            String comando = parts[0].toUpperCase();

            switch (comando) {
                case "LOOKUP":
                    if (parts.length < 2) {
                        out.println("ERRO: Hostname não especificado");
                        logManager.warning("LOOKUP sem hostname de " + clientAddress);
                        return;
                    }
                    String ip = hostTable.get(parts[1]);
                    logManager.info("LOOKUP de " + clientAddress + ": " + parts[1] + " -> " +
                            (ip != null ? ip : "não encontrado"));
                    out.println(ip != null ? ip : "Não encontrado");
                    break;

                case "REGISTER":
                    if (parts.length < 3) {
                        out.println("ERRO: Formato correto é REGISTER <hostname> <ip>");
                        logManager.warning("REGISTER com parâmetros insuficientes de " + clientAddress);
                        return;
                    }
                    boolean sucesso = registerHost(parts[1], parts[2]);
                    logManager.info("REGISTER de " + clientAddress + ": " + parts[1] + " -> " +
                            parts[2] + " (" + (sucesso ? "sucesso" : "falha") + ")");
                    out.println(sucesso ? "Registrado" : "Erro: já existe");
                    break;

                default:
                    logManager.warning("Comando inválido de " + clientAddress + ": " + comando);
                    out.println("Comando inválido");
            }

        } catch (IOException e) {
            logManager.severe("Erro com cliente " + clientAddress + ": " + e.getMessage());
        }
        finally {
            logManager.fine("Conexão encerrada com " + clientAddress);
        }
    }
}

