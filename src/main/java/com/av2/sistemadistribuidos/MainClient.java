package com.av2.sistemadistribuidos;

import java.util.Scanner;

/**
 * Classe principal do cliente que provê interface de linha de comando
 * - Permite ao usuário registrar hostnames e IPs
 * - Permite consultar IPs por hostname
 * - Loop principal que processa comandos REGISTER e LOOKUP
 * - Usa a classe Resolver para comunicação com o servidor
 */
public class MainClient {
    private static final LogManager logManager = LogManager.getInstance();

    public static void main(String[] args) {
        logManager.info("Iniciando cliente DNS");

        Resolver resolver = new Resolver("localhost", 12345);
        Scanner scanner = new Scanner(System.in);

        logManager.info("Cliente iniciado - aguardando comandos");
        System.out.println("Digite os comandos (LOOKUP <hostname> ou REGISTER <hostname> <ip>):");

        while (true) {
            System.out.print("> ");
            String linha = scanner.nextLine().trim();

            if (linha.isEmpty()) {
                continue;
            }

            if ("SAIR".equalsIgnoreCase(linha)) {
                logManager.info("Encerrando cliente");
                break;
            }

            String[] partes = linha.split("\\s+");
            try {
                processarComando(partes, resolver);
            } catch (Exception e) {
                logManager.severe("Erro ao processar comando: " + e.getMessage());
                System.out.println("Erro: " + e.getMessage());
            }
        }

        scanner.close();
        logManager.info("Cliente encerrado");
    }

    private static void processarComando(String[] partes, Resolver resolver) {
        if (partes.length < 2) {
            logManager.warning("Comando inválido recebido");
            System.out.println("Comando inválido");
            return;
        }

        String comando = partes[0].toUpperCase();
        String hostname = partes[1];

        switch (comando) {
            case "LOOKUP":
                logManager.fine("Processando comando LOOKUP para: " + hostname);
                String ip = resolver.lookup(hostname);
                System.out.println("Resposta: " + ip);
                break;

            case "REGISTER":
                if (partes.length != 3) {
                    logManager.warning("Comando REGISTER com número incorreto de parâmetros");
                    System.out.println("Uso: REGISTER <hostname> <ip>");
                    return;
                }
                String novoIp = partes[2];
                logManager.fine("Processando comando REGISTER para: " + hostname + " -> " + novoIp);
                String resultado = resolver.register(hostname, novoIp);
                System.out.println("Resposta: " + resultado);
                break;

            default:
                logManager.warning("Comando desconhecido recebido: " + comando);
                System.out.println("Comando desconhecido");
     }
    }
}

