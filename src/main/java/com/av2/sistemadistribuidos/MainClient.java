package com.av2.sistemadistribuidos;

import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        Resolver resolver = new Resolver("localhost", 12345);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite comandos (REGISTER nome ip ou LOOKUP nome). 'sair' para encerrar.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("sair")) break;

            String[] parts = line.split(" "); // Divide em parte usando o espaço.
            if (parts.length == 2 && parts[0].equalsIgnoreCase("LOOKUP")) {
                String result = resolver.lookup(parts[1]);
                System.out.println("Resposta: " + result);
            } else if (parts.length == 3 && parts[0].equalsIgnoreCase("REGISTER")) {
                String result = resolver.register(parts[1], parts[2]);
                System.out.println("Resposta: " + result);
            } else {
                System.out.println("Comando inválido.");
            }
        }
        scanner.close();
    }
}


