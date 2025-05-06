package com.av2.sistemadistribuidos;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class nomeServidor {
    private int port;
    private Map<String, String> hostTable = new HashMap<>();

    public nomeServidor(int port) {
        this.port = port;
    }

    // Adiciona um novo nome e IP no servidor
    public void registerHost(String hostname, String ip) {
        hostTable.put(hostname, ip);
        System.out.println("Registrado: " + hostname + " -> " + ip);
    }

    // Inicia o servidor e escuta pedidos
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor de nomes iniciado na porta " + port);

        // Loop para aceitar múltiplas conexões
        while (true) {
            Socket client = serverSocket.accept(); // Espera uma conexão
            new Thread(() -> handleClient(client)).start(); // Cria nova thread para cada cliente
        }
    }

    // Processa a requisição do cliente
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request = in.readLine();
            String[] parts = request.split(" ");

            if (parts[0].equalsIgnoreCase("LOOKUP") && parts.length == 2) {
                String ip = hostTable.get(parts[1]);
                out.println(ip != null ? ip : "Não encontrado");
            } else if (parts[0].equalsIgnoreCase("REGISTER") && parts.length == 3) {
                hostTable.put(parts[1], parts[2]);
                out.println("REGISTRADO");
                System.out.println("Novo registro: " + parts[1] + " -> " + parts[2]);
            } else {
                out.println("Comando invalido");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

