package com.av2.sistemadistribuidos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {
    private static final Map<Integer, ConfigManager> instances = new ConcurrentHashMap<>();
    private final Properties properties;
    private final LogManager logManager;
    private final int porta;


    private ConfigManager(int porta) {
        this.porta = porta;
        this.properties = new Properties();
        this.logManager = LogManager.getInstance(porta);
        loadProperties();
    }

    /*

   Problema de condição de corrida: Sem thread-safety, pode rolar o seguinte problema:
    - Thread 1 verifica "if (instance == null) -> true"
    - Thread 2 verifica "if (instance == null) -> true"
    - Thread 1 cria instância
    - Thread 2 cria outra instância
    - Agora temos duas instâncias diferentes com possíveis configurações diferentes

    1. Consistência de Dados**: O servidor precisa garantir que todas as threads
     estejam, usando as mesmas configurações. Por exemplo:

    - Se uma thread vê um timeout de 1000ms
    - E outra vê 2000ms
    - Isso pode causar comportamentos inconsistentes no servidor

    1. O méto-do `computeIfAbsent` do já é thread-safe por natureza.
     Ele garante atomicidade na operação de verificar se a chave existe e,
      caso não exista, criar uma nova entrada. `ConcurrentHashMap`

    - A consistência é garantida pelo `ConcurrentHashMap`
    - Cada porta terá sua única instância de `ConfigManager`
    - As configurações carregadas permanecerão consistentes para cada porta


    Obviamente como isso não é um codigo comercial, será quase impossivel ocorrer esses problemas,
     desde que não se mexa nas configurações, porém serve de aprendizado.


 */
    public static ConfigManager getInstance(int porta) {
        return instances.computeIfAbsent(porta, ConfigManager::new);
    }



    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config_" + porta + ".properties")) {
            if (input == null) {
                // Se não encontrar arquivo específico, carrega o padrão
                try (InputStream defaultInput = getClass().getClassLoader()
                        .getResourceAsStream("config.properties")) {
                    if (defaultInput != null) {
                        properties.load(defaultInput);
                    } else {
                        logManager.severe("Arquivo config.properties não encontrado!");
                    }
                }
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            logManager.severe("Erro ao carregar configurações: " + e.getMessage());
        }
    }

    public String getFilePath() {
        return properties.getProperty("server.file.path", "src/main/resources/registros_" + porta + ".txt");
    }

    public int getServerPort() {

        return Integer.parseInt(properties.getProperty("server.port", String.valueOf(porta)));

    }



    public int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("server.thread.pool.size", "10"));
    }

    public int getServerTimeout() {
        return Integer.parseInt(properties.getProperty("server.timeout", "1000"));
    }
}
