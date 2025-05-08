package com.av2.sistemadistribuidos;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class nomeServidor {
    private final int port;
    private final Map<String, String> hostTable = new HashMap<>();
    private static final String FILE_NAME = "src/main/resources/registros.txt";
    private static final Logger logger = Logger.getLogger(nomeServidor.class.getName());
    private final ExecutorService executorService;
    private volatile boolean isRunning = true;

    public nomeServidor(int port) {
        this.port = port;
        // Cria um pool de threads com número fixo de threads
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // Adiciona um novo nome e IP no servidor
    public boolean registerHost(String hostname, String ip) {
        synchronized (hostTable) {
            // Verifica se o hostname ou IP já existe na tabela em memória
            if (hostTable.containsKey(hostname) || hostTable.containsValue(ip)) {
                logger.warning("Hostname ou IP já existe na tabela: " + hostname + " -> " + ip);
                return false;
            }

            // Se não existir duplicata, registra na tabela e no arquivo
            hostTable.put(hostname, ip);
            appendToFile(hostname, ip);
            System.out.println("Registrado: " + hostname + " -> " + ip);
            return true;
        }
    }


    // Inicia o servidor e escuta pedidos
    public void start() {
        loadFromFile(); // Carrega registros salvos do arquivo

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor de nomes iniciado na porta " + port);
            serverSocket.setSoTimeout(1000); // Timeout de 1 segundo para accept()

            while (isRunning) {
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

    // Método para desligar o servidor graciosamente
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
    }

    private void handleClient(Socket clientSocket) {
    try (
        clientSocket;
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
    ) {
        String request = in.readLine();
        System.out.println("[DEBUG] Recebido do cliente: " + request);

        if (request == null || request.trim().isEmpty()) {
            out.println("Comando invalido");
            return;
        }

        String[] parts = request.trim().split("\\s+");
        
        if (parts.length == 2 && parts[0].equalsIgnoreCase("LOOKUP")) {
            String ip = hostTable.get(parts[1]);
            out.println(ip != null ? ip : "Não encontrado");

        } else if (parts.length == 3 && parts[0].equalsIgnoreCase("REGISTER")) {
            boolean registrado = registerHost(parts[1], parts[2]);
            out.println(registrado ? "Registrado" : "Hostname ou IP já existe");
        } else {
            out.println("Comando Inválido");
        }

    } catch (IOException e) {
        logger.severe("Erro ao processar cliente: " + e.getMessage());
    }
}

    private void appendToFile(String hostname, String ip) {
        synchronized (hostTable) {
            // Verifica se o arquivo existe, se não, cria
            File file = new File(FILE_NAME);
            try {
                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        logger.severe("Falha ao criar diretórios necessários");
                        return;
                    }
                }

                if (!file.exists() && !file.createNewFile()) {
                    logger.severe("Falha ao criar arquivo de registros");
                    return;
                }
            } catch (IOException e) {
                logger.severe("Erro ao criar arquivo: " + e.getMessage());
                return;
            }


            // Verifica duplicatas
            if (HostOuIpExiste(hostname, ip)) {
                logger.warning("Hostname ou IP já existe no arquivo: " + hostname + " -> " + ip);
                return;
            }


            // Se não existir duplicata, adiciona ao arquivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                writer.write(hostname + " " + ip);
                writer.newLine();
                System.out.println("Registro salvo com sucesso: " + hostname + " -> " + ip);
            } catch (IOException e) {
                logger.severe("Erro ao salvar no registro: " + e.getMessage());
            }
        }
    }


    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    hostTable.put(parts[0], parts[1]);
                    System.out.println("Carregado: " + parts[0] + " -> " + parts[1]);
                }
            }
        } catch (IOException e) {
            logger.severe("Erro ao carregar registros: " + e.getMessage());
        }
    }
    private boolean HostOuIpExiste(String hostname, String ip) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    if (parts[0].equals(hostname) || parts[1].equals(ip)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Erro ao verificar duplicatas: " + e.getMessage());
        }
        return false;
    }

}