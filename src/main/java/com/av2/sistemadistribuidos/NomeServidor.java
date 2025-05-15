package com.av2.sistemadistribuidos;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import com.av2.sistemadistribuidos.data.DataStore;
import com.av2.sistemadistribuidos.model.Usuario;

public class NomeServidor {
    private final int port;
    private static final Logger logger = Logger.getLogger(NomeServidor.class.getName());
    private final ExecutorService executorService;
    private volatile boolean isRunning = true;
    private final DataStore dataStore;

    public NomeServidor(int port) {
        this.port = port;
        this.dataStore = new DataStore.Builder()
            .withHostsDAO("src/main/resources/registros.txt")
            .withUsersDAO("src/main/resources/usuarios.txt")
            .build();

        // Cria um pool de threads com número fixo de threads
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // Adiciona um novo nome e IP no servidor
    public boolean registerHost(String hostname, String ip) {
        this.dataStore.hosts().append(hostname, ip);
        return true;

    }

    // Inicia o servidor e escuta pedidos
    public void start() {
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
        InputStream in = clientSocket.getInputStream();
        OutputStream out = clientSocket.getOutputStream();
    ) {
        byte[] buffer = new byte[1024];
        int bytesRead = in.read(buffer);
        if (bytesRead != -1) {
            String request = new String(buffer, 0, bytesRead).trim();
            System.out.println("[DEBUG] Recebido do cliente: " + request);

            String[] parts = request.split("\\s+");

            switch (parts[0].toUpperCase()) {
                case "LOOKUP" -> {
                    if (parts.length != 2) {
                        out.write("Comando Inválido".getBytes());
                        break;
                    }
                    String ip = dataStore.hosts().lookupHost(parts[1]);
                    String response = ip != null ? ip : "Não encontrado";
                    out.write(response.getBytes());
                }

                case "REGISTER" -> {
                    if (parts.length != 5) {
                        out.write("Comando Inválido".getBytes());
                        break;
                    }
                    Usuario usuario = new Usuario(parts[1], parts[2], dataStore.users());
                    if (usuario.authenticate()) {
                        boolean registrado = registerHost(parts[3], parts[4]);
                        String response = registrado ? "Registrado" : "Hostname ou IP já existe";
                        out.write(response.getBytes());
                    } else {
                        out.write("Não autenticado".getBytes());
                    }
                }

                case "USER" -> {
                    if (parts.length != 3) {
                        out.write("Comando Inválido".getBytes());
                        break;
                    }
                    Usuario usuario = new Usuario(parts[1], parts[2], dataStore.users());
                    if (usuario.register()) {
                        out.write("Novo Usuário cadastrado".getBytes());
                    } else {
                        out.write("Erro ao cadastrar usuário".getBytes());
                    }
                }

                default -> out.write("Comando Inválido".getBytes());
            }
            out.flush();
        } else {
            System.out.println("[DEBUG] nada recebido");
        }

        } catch (IOException e) {
            logger.severe("Erro ao processar cliente: " + e.getMessage());
        }
    }

}







