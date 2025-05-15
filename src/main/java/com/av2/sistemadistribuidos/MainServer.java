package com.av2.sistemadistribuidos;
import java.util.logging.Logger;

public class MainServer {
    public static void main(String[] args) {
        final Logger logger = Logger.getLogger(MainServer.class.getName());
        NomeServidor server = new NomeServidor(12345); // Porta do servidor
        server.registerHost("hostA.local", "192.168.0.1");
        server.registerHost("hostB.local", "192.168.0.2");
        server.registerHost("hostC.local", "192.168.0.3");
        server.registerHost("hostD.local", "192.168.0.4");
        server.registerHost("hostE.local", "192.168.0.5");
        server.registerHost("hostF.local", "192.168.0.6");
        server.registerHost("hostG.local", "192.168.0.7");
        server.registerHost("hostH.local", "192.168.0.8");
        server.registerHost("hostI.local", "192.168.0.9");
        server.registerHost("hostJ.local", "192.168.0.10");
        server.registerHost("hostK.local", "192.168.0.11");
        server.registerHost("hostL.local", "192.168.0.12");
        server.registerHost("hostM.local", "192.168.0.13");
        server.registerHost("hostN.local", "192.168.0.14");
        server.registerHost("hostO.local", "192.168.0.15");
        server.registerHost("hostP.local", "192.168.0.16");
        server.registerHost("hostQ.local", "192.168.0.17");
        server.registerHost("hostR.local", "192.168.0.18");

        // Alguns exemplos basicos para teste.

        try {
            server.start(); // Inicia o servidor
        } catch (Exception e) {
            logger.severe("Erro ao aceitar conexão: " + e.getMessage());
        }
    }
}

// Se possivel no futuro adicionar persistencia de arquivos no servidor, para manter os DNS e IPs já registrados

