
package com.av2.sistemadistribuidos;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

public class FileManager {
    private final String filePath;
    private final ReentrantReadWriteLock lock;
    private final LogManager logManager;

    // Padrão para validar endereços IP
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    // Padrão para validar hostnames
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9]([a-zA-Z0-9\\-.]{0,61}[a-zA-Z0-9])?$"
    );

    public FileManager(String filePath, int porta) {
        // Adiciona a porta ao nome do arquivo
        this.filePath = filePath + "_" + porta;
        this.lock = new ReentrantReadWriteLock();
        this.logManager = LogManager.getInstance(porta);
        criarArquivoSeNecessario();
    }

    private void criarArquivoSeNecessario() {
        lock.writeLock().lock();
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                logManager.info("Arquivo de registros criado: " + filePath);
            }
        } catch (IOException e) {
            logManager.severe("Erro ao criar arquivo de registros: " + e.getMessage());
            throw new RuntimeException("Não foi possível criar o arquivo de registros", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean isHostnameInvalido(String hostname) {
        return hostname == null ||
                hostname.trim().isEmpty() ||
                !HOSTNAME_PATTERN.matcher(hostname).matches() ||
                hostname.length() > 253;
    }


    private boolean isIPInvalido(String ip) {
        return ip == null || !IP_PATTERN.matcher(ip).matches();
    }


    public void salvarRegistro(String hostname, String ip) {
        // Validação inicial dos dados
        if (isHostnameInvalido(hostname.trim())) {
            logManager.warning("Hostname inválido: " + hostname);
            throw new IllegalArgumentException("Formato de hostname inválido");
        }

        if (isIPInvalido(ip.trim())) {
            logManager.warning("IP inválido: " + ip);
            throw new IllegalArgumentException("Formato de IP inválido");
        }


        lock.writeLock().lock();
        try {
            logManager.info("Salvando registro: " + hostname + " -> " + ip);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                String registro = hostname.trim() + " " + ip.trim();
                writer.write(registro);
                writer.newLine();
                writer.flush();

                logManager.fine("Registro salvo com sucesso: " + registro);
            } catch (IOException e) {
                logManager.severe("Falha ao salvar registro: " + e.getMessage());
                throw new RuntimeException("Erro ao salvar no arquivo", e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void carregarRegistros(Map<String, String> hostTable) {
        lock.readLock().lock();
        try {
            logManager.info("Iniciando carregamento de registros do arquivo");
            hostTable.clear();

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String linha;
                int numeroLinha = 0;

                while ((linha = reader.readLine()) != null) {
                    numeroLinha++;
                    linha = linha.trim();

                    // Pula linhas vazias e comentários
                    if (linha.isEmpty() || linha.startsWith("#")) {
                        continue;
                    }

                    String[] partes = linha.split("\\s+");
                    if (partes.length != 2) {
                        logManager.warning("Linha " + numeroLinha + " inválida (formato incorreto): " + linha);
                        continue;
                    }

                    String hostname = partes[0].trim();
                    String ip = partes[1].trim();

                    // Validação do hostname
                    if (isHostnameInvalido(hostname)) {
                        logManager.warning("Linha " + numeroLinha + " inválida (hostname inválido): " + linha);
                        continue;
                    }


                    // Validação do IP
                    if (isIPInvalido(ip)) {
                        logManager.warning("Linha " + numeroLinha + " inválida (IP inválido): " + linha);
                        continue;
                    }


                    // Verifica duplicatas
                    if (hostTable.containsKey(hostname)) {
                        logManager.warning("Hostname duplicado encontrado na linha " + numeroLinha + ": " + hostname);
                        continue;
                    }

                    if (hostTable.containsValue(ip)) {
                        logManager.warning("IP duplicado encontrado na linha " + numeroLinha + ": " + ip);
                        continue;
                    }

                    hostTable.put(hostname, ip);
                    logManager.fine("Registro carregado: " + hostname + " -> " + ip);
                }

                logManager.info("Carregamento concluído. Total de registros válidos: " + hostTable.size());

            } catch (IOException e) {
                logManager.severe("Erro ao carregar registros: " + e.getMessage());
                throw new RuntimeException("Erro ao ler arquivo de registros", e);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
}
