package com.av2.sistemadistribuidos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigManager {
    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());
    private static final Properties properties = new Properties();
    private static ConfigManager instance;

    private ConfigManager() { // impede criação direta de instancia
        loadProperties();
    }
 /*
   O metodo getInstance DEVE SER SEMPRE SINCRONIZADO para evitar problemas de condição de corrida E consistencia de dados
   lembrando que estamos a simular um servidor
   Exemplos de possiveis problemas caso não esteja sincronizado:

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


    Obviamente como isso não é um codigo comercial, será quase impossivel ocorrer esses problemas,
     desde que não se mexa nas configurações, porém serve de aprendizado.


 */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) { /* garante a integridade das configurações para que a instância seja unica
        ja que a mesma gerencia conexões, quantidade de threads e caminho do arquivo
        */
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.severe("Arquivo config.properties não encontrado!");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            logger.severe("Erro ao carregar configurações: " + e.getMessage());
        }
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "12345"));
    }

    public String getFilePath() {
        return properties.getProperty("server.file.path", "src/main/resources/registros.txt");
    }

    public int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("server.thread.pool.size", "10"));
    }

    public int getServerTimeout() {
        return Integer.parseInt(properties.getProperty("server.timeout", "1000"));
    }
}
