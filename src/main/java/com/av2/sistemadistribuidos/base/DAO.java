package com.av2.sistemadistribuidos.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class DAO {
    protected final Properties properties = new Properties();
    protected final String filePath;
    protected static final Logger logger = Logger.getLogger(DAO.class.getName());
    private final String name;

    protected DAO(String filePath, String name) {
        this.filePath = filePath;
        this.name = name;
        ensureDataDirectory();
        loadData();
    }

    private void ensureDataDirectory() {
        new File("data").mkdirs();
    }

    protected void loadData() {
        try (FileInputStream in = new FileInputStream(filePath)) {
            properties.load(in);
            logger.info(name + " loaded: " + properties.size() + " entries");
        } catch (IOException e) {
            logger.info("No existing file found for " + name);
        }
    }

    protected boolean saveData() {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            properties.store(out, name + " Database");
            logger.info(name + " saved successfully");
            return true;
        } catch (IOException e) {
            logger.severe("Failed to save " + name + ": " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean append(String key, String value) {
        if (properties.containsKey(key)) {
            logger.warning(key + " already exists in " + name);
            return false;
        }

        properties.setProperty(key, value);
        return saveData();
    }

    public Map<String, String> getAll() {
        Map<String, String> result = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            result.put(key, properties.getProperty(key));
        }
        return result;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}