package com.av2.sistemadistribuidos;
import java.io.File;
import java.sql.SQLOutput;
import java.util.logging.Logger;

/**
 * Classe principal do servidor
 * - Inicializa o servidor de nomes
 * - Configura o logger para registro de erros
 * - Gerencia o ciclo de vida do servidor
 */

public class MainServer {
    public static void main(String[] args) {
        final Logger logger = Logger.getLogger(MainServer.class.getName());
        nomeServidor server = new nomeServidor();
        // Porta do servidor

        try {
            server.start(); // Inicia o servidor
        } catch (Exception e) {
            logger.severe("Erro ao aceitar conexão: " + e.getMessage());
        }
    }
}


