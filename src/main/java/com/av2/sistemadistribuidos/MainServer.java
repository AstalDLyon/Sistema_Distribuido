package com.av2.sistemadistribuidos;
import java.util.logging.Logger;

public class MainServer {
    public static void main(String[] args) {
        final Logger logger = Logger.getLogger(MainServer.class.getName());
        nomeServidor server = new nomeServidor(12345); // Porta do servidor

        try {
            server.start(); // Inicia o servidor
        } catch (Exception e) {
            logger.severe("Erro ao aceitar conexão: " + e.getMessage());
        }
    }
}


