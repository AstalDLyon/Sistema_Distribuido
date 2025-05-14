package com.av2.sistemadistribuidos;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.*;


public class nomeServidor {
    private final int port; // port: porta em que o servidor escuta
    private final Map<String, String> hostTable = new HashMap<>(); // hostTable: mapa que armazena pares hostname->IP
    private static final Logger logger = Logger.getLogger(nomeServidor.class.getName());
    private final ExecutorService executorService; // executorService: gerencia pool de threads para requisições concorrentes
    private volatile boolean isRunning = true;
    private final FileManager fileManager; // fileManager: gerencia persistência dos registros
    private final ConfigManager config; // config: gerencia configurações do servidor


    public nomeServidor() {
        this.config = ConfigManager.getInstance();
        this.port = config.getServerPort();
        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());
        this.fileManager = new FileManager(config.getFilePath());

        // Configuração do logger para arquivo
        configureLogger();
    }

    //  Registra novo par hostname->IP
    //  Verifica duplicatas
    //  Persiste registro em arquivo
    //  Thread-safe usando synchronized
    public boolean registerHost(String hostname, String ip) {
        synchronized (hostTable) {
            // Verifica se o hostname ou IP já existe na tabela em memória
            if (hostTable.containsKey(hostname) || hostTable.containsValue(ip)) {
                logger.warning("Hostname ou IP já existe na tabela: " + hostname + " -> " + ip);
                return false;
            }

            // Se não existir duplicata, registra na tabela e no arquivo
            hostTable.put(hostname, ip);
            fileManager.salvarRegistro(hostname, ip);
            System.out.println("Registrado: " + hostname + " -> " + ip);
            logger.info(String.format("Registrado: %s -> %s", hostname, ip));
            return true;
        }
    }


    // Inicia o servidor e escuta pedidos
    public void start() {
        fileManager.carregarRegistros(hostTable);// Carrega registros salvos do arquivo

        try (ServerSocket serverSocket = new ServerSocket(port)) { // Inicia socket servidor

            System.out.println("Servidor iniciado na porta " + port);
            logger.info(String.format("Servidor iniciado na porta " + port));
            serverSocket.setSoTimeout(config.getServerTimeout());// Timeout de 1 segundo para accept()

            while (isRunning) { // Aceita conexões de clientes

                try {
                    Socket client = serverSocket.accept();
                    executorService.submit(() -> handleClient(client));
                } catch (SocketTimeoutException e) {
                    // Timeout normal, continua o loop

                } catch (IOException e) {
                    logger.severe("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.severe("Erro fatal ao iniciar o servidor: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    //Méto-do para desligar o servidor
    public void shutdown() {
        isRunning = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Servidor encerrado");
        logger.info("Servidor encerrado");
    }

    //handleClient(clientSocket):
    // - Processa requisições individuais
    //  Parse de comandos LOOKUP e REGISTER
    // Envia respostas ao cliente

    private void handleClient(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        logger.info("Nova conexão de: " + clientAddress);

        try (
                clientSocket;
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request = in.readLine();
            logger.fine("Requisição recebida de " + clientAddress + ": " + request);
            System.out.println("[DEBUG] Recebido do cliente: " + request);

            if (request == null || request.trim().isEmpty()) {
                logger.warning("Requisição vazia de " + clientAddress);
                out.println("Comando invalido");
                return;
            }

            String[] parts = request.trim().split("\\s+");

            if (parts.length == 2 && parts[0].equalsIgnoreCase("LOOKUP")) {
                logger.info("Processando LOOKUP para: " + parts[1]);
                String ip = hostTable.get(parts[1]);
                out.println(ip != null ? ip : "Não encontrado");
                logger.info("Resposta para LOOKUP: " + (ip != null ? ip : "Não encontrado"));

            } else if (parts.length == 3 && parts[0].equalsIgnoreCase("REGISTER")) {
                logger.info("Processando REGISTER: " + parts[1] + " -> " + parts[2]);
                boolean registrado = registerHost(parts[1], parts[2]);
                out.println(registrado ? "Registrado" : "Hostname ou IP já existe");
                logger.info("Resultado do REGISTER: " + (registrado ? "Sucesso" : "Falha - Duplicado"));
            } else {
                logger.warning("Comando inválido recebido: " + request);
                out.println("Comando Inválido");
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao processar cliente " + clientAddress, e);
        } finally {
            logger.info("Conexão com " + clientAddress + " encerrada");
        }
    }

    private void configureLogger() {
        try {
            // 1. Garante que o diretório de logs existe
            Path logsDir = Paths.get("logs");
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }

            // 2. Gera nome do arquivo com data atual
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String logFileName = "servidor_" + currentDate + ".txt"; // Nome fixo por dia

            // 3. Configuração especial para evitar a numeração automática
            FileHandler fileHandler = new FileHandler(
                    logsDir.resolve(logFileName).toString(),
                    true // Apenas append (não rotaciona automaticamente)
            );

            // 4. Formatação customizada
            fileHandler.setFormatter(new SimpleFormatter() {
                private final SimpleDateFormat dateFormat =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public synchronized String format(LogRecord record) {
                    return String.format("[%s] [%s] %s%n",
                            dateFormat.format(new Date(record.getMillis())),
                            record.getLevel().getName(),
                            record.getMessage());
                }
            });

            // 5. Configuração do logger
            logger.setUseParentHandlers(false); // Remove o handler padrão
            logger.addHandler(fileHandler);

            logger.info("Arquivo de log inicializado: " + logFileName);

        } catch (IOException e) {
            System.err.println("Erro crítico ao configurar logs: " + e.getMessage());
        }
    }



}