package com.av2.sistemadistribuidos;

import java.io.;
import java.net.;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServidorReplicado extends NomeServidor {
    private final List<EnderecoServidor> servidoresSecundarios;
    private final LogManager logManager;
    private final boolean isPrimario;

    private record EnderecoServidor(String host, int porta) {}

    public ServidorReplicado(boolean isPrimario, int porta) {
        super(porta);
        this.logManager = LogManager.getInstance(porta);
        this.isPrimario = isPrimario;
        this.servidoresSecundarios = new CopyOnWriteArrayList<>();
        logManager.info("Servidor Replicado inicializado como " +
                (isPrimario ? "PRIMÁRIO" : "SECUNDÁRIO") + " na porta" + porta);
    }

    public void adicionarServidorSecundario(String host, int porta) {
        if (isPrimario) {
            servidoresSecundarios.add(new EnderecoServidor(host, porta));
            logManager.info("Servidor secundário adicionado: " + host + ":" + porta);
        }
    }

    @Override
    protected boolean registerHost(String hostname, String ip) {
        boolean resultado = super.registerHost(hostname, ip);

        if (resultado && isPrimario) {
            replicarRegistro(hostname, ip);
        }

        return resultado;
    }

    private void replicarRegistro(String hostname, String ip) {  // Méto-do responsável por replicar registros para servidores secundários
        // Implementa mecanismo de tolerância a falhas
        // Timeout configurado para 5 segundos por tentativa

        for (EnderecoServidor servidor : servidoresSecundarios) {
            try (Socket socket = new Socket(servidor.host(), servidor.porta())) {
                socket.setSoTimeout(5000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out.println("REPLICATE " + hostname + " " + ip);
                String resposta = in.readLine();

                if (!"OK".equals(resposta)) {
                    logManager.warning("Falha ao replicar para " +
                            servidor.host() + ":" + servidor.porta());
                }
            } catch (IOException e) {
                logManager.severe("Erro ao replicar para " +
                        servidor.host() + ":" + servidor.porta() +
                        ": " + e.getMessage());
            }
        }
    }

    @Override
    protected void handleClient(Socket clientSocket) {
        if (!isPrimario) {
            processarComandoReplicacao(clientSocket);
        } else {
            super.handleClient(clientSocket);
        }
    }

    private void processarComandoReplicacao(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request != null && request.startsWith("REPLICATE")) {
                String[] parts = request.split("\s+");
                if (parts.length == 3) {
                    boolean sucesso = super.registerHost(parts[1], parts[2]);
                    out.println(sucesso ? "OK" : "ERRO");
                } else {
                    out.println("ERRO: Formato inválido");
                }
            } else {
                out.println("ERRO: Comando não permitido em servidor secundário");
            }
        } catch (IOException e) {
            logManager.severe("Erro ao processar replicação: " + e.getMessage());
        }
    }
}