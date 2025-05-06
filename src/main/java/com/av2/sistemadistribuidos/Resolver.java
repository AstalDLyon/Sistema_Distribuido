package com.av2.sistemadistribuidos;

import java.io.*;
import java.net.*;

public class Resolver {
    private String serverAddress;
    private int serverPort;

    public Resolver(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public String sendCommand(String command) {
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            out.println(command);
            return in.readLine();
        } catch (IOException e) {
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



