package com.av2.sistemadistribuidos;

import java.io.*;
import java.util.Map;
import java.util.logging.Logger;

public class FileManager {
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());
    private final String fileName;
    
    public FileManager(String fileName) {
        this.fileName = fileName;
    }

    /* salvarRegistro(hostname, ip):
        Salva novo registro no arquivo
        Thread-safe usando synchronized
        Verifica duplicatas antes de salvar
*/
    public void salvarRegistro(String hostname, String ip) {
        synchronized (this) {
            criarArquivoSeNecessario();
            
            if (verificarDuplicata(hostname, ip)) {
                logger.warning("Hostname ou IP já existe no arquivo: " + hostname + " -> " + ip);
                return;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                writer.write(hostname + " " + ip);
                writer.newLine();
                System.out.println("Registro salvo com sucesso: " + hostname + " -> " + ip);
            } catch (IOException e) {
                logger.severe("Erro ao salvar no registro: " + e.getMessage());
            }
        }
    }

    /* carregarRegistros(hostTable):
        Carrega registros salvos para memória
        Popula hostTable com dados do arquivo
*/
    public void carregarRegistros(Map<String, String> hostTable) {
        File file = new File(fileName);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    hostTable.put(parts[0], parts[1]);
                    System.out.println("Carregado: " + parts[0] + " -> " + parts[1]);
                }
            }
        } catch (IOException e) {
            logger.severe("Erro ao carregar registros: " + e.getMessage());
        }
    }

    /*
    verificarDuplicata(hostname, ip):
    Verifica se hostname ou IP já existe
    Previne registros duplicados
     */
    private boolean verificarDuplicata(String hostname, String ip) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    if (parts[0].equals(hostname) || parts[1].equals(ip)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Erro ao verificar duplicatas: " + e.getMessage());
        }
        return false;
    }

    /*  criarArquivoSeNecessario():
        Cria arquivo e diretórios se não existirem
        Garante existência da estrutura necessária
     */

    private void criarArquivoSeNecessario() {
        File file = new File(fileName);
        try {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    logger.severe("Falha ao criar diretórios necessários");
                    return;
                }
            }

            if (!file.exists() && !file.createNewFile()) {
                logger.severe("Falha ao criar arquivo de registros");
            }
        } catch (IOException e) {
            logger.severe("Erro ao criar arquivo: " + e.getMessage());
        }
    }
}