package com.av2.sistemadistribuidos;

import java.io.*;
import java.net.*;

// O resolver é a parte mais importante do codigo no momento, tome cuidado
public class Resolver {
    private final String serverAddress;
    private final int serverPort;
    private final LogManager logManager;


    public Resolver(String serverAddress, int serverPort) {
        this.logManager = LogManager.getInstance();
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        logManager.info(String.format("Resolver iniciado com endereço %s:%d", serverAddress, serverPort));
    }

    // sendCommand(command):
    // Estabelece conexão socket com servidor
    // Envia comando
    // Recebe e retorna resposta

    public String sendCommand(String command) {
        logManager.fine("Iniciando envio de comando: " + command);

        Socket socket = null;
        int tentativas = 0;
        final int MAX_TENTATIVAS = 3;

        while (tentativas < MAX_TENTATIVAS) {
            try {
                socket = new Socket();
                socket.connect(
                        new InetSocketAddress(serverAddress, serverPort),
                        5000  // 5 segundos de timeout
                );
                logManager.fine("Conexão estabelecida com servidor");

                try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream())
                     )) {

                    out.println(command);
                    logManager.fine("Comando enviado: " + command);

                    String response = in.readLine();
                    logManager.fine("Resposta recebida: " + response);

                    return response;
                }
            } catch (IOException e) {
                tentativas++;
                logManager.warning(String.format(
                        "Tentativa %d de %d falhou: %s",
                        tentativas, MAX_TENTATIVAS, e.getMessage()
                ));

                if (tentativas == MAX_TENTATIVAS) {
                    String errorMsg = "Erro de comunicação após " +
                            MAX_TENTATIVAS + " tentativas: " + e.getMessage();
                    logManager.severe(errorMsg);
                    return "ERRO: " + errorMsg;
                }

                try {
                    Thread.sleep(1000); // Espera 1 segundo antes de tentar novamente
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } finally {
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logManager.warning("Erro ao fechar socket: " + e.getMessage());
                    }
                }
            }
        }
        return "ERRO: Máximo de tentativas excedido";
    }



    // lookup(hostname):
    //  Envia comando LOOKUP
    // Retorna IP associado ao hostname
    public String lookup(String hostname) {
        logManager.info("Executando LOOKUP para hostname: " + hostname);
        String response = sendCommand("LOOKUP " + hostname);
        logManager.info("LOOKUP completado - Resultado: " + response);
        return response;
    }


    // register(hostname, ip):
    // Envia comando REGISTER
    // Registra novo par hostname->IP

    public String register(String hostname, String ip) {
        logManager.info("Executando REGISTER para " + hostname + " -> " + ip);
        String response = sendCommand("REGISTER " + hostname + " " + ip);
        logManager.info("REGISTER completado - Resultado: " + response);
        return response;
    }
}



