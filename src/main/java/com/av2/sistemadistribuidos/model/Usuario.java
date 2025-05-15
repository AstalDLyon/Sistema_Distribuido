package com.av2.sistemadistribuidos.model;

import com.av2.sistemadistribuidos.data.UsersDAO;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Usuario {
    String login;
    String password;
    private final UsersDAO usersDAO;

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public Usuario(String login, String password, UsersDAO usersDAO){
        this.login = login;
        this.password = password;
        this.usersDAO = usersDAO;
    }

    public Boolean authenticate(){
        String hash = usersDAO.get(this.login);
        if(hash.equals(this.hashPassword(password))){
            return true;
        }

        return false;
    }

    public boolean register(){
        String hash = this.hashPassword(this.password);

        usersDAO.append(this.login, hash);

        return true;
    }

};