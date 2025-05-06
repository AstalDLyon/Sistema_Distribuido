package com.av2.sistemadistribuidos;

import java.io.*;
import java.net.*;

// O resolver é a parte mais importante do codigo no momento, tome cuidado
public class Resolver {
    private String serverAddress;
    private int serverPort;

    public Resolver(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public String sendCommand(String command) {
        try (
                Socket socket = new Socket(serverAddress, serverPort); // Estabelece uma conexão usando socket
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // para enviar dados para o servidor
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // para ouvir a resposta
        ) {
            out.println(command);
            return in.readLine();
        } catch (IOException e) { // capturar a mensagem de erro
            return "ERRO: " + e.getMessage();
        }
    }

    public String lookup(String hostname) {
        return sendCommand("LOOKUP " + hostname);
    }

    public String register(String hostname, String ip) {
        return sendCommand("REGISTER " + hostname + " " + ip);
    }
}



