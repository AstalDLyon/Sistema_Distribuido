package com.av2.sistemadistribuidos;

import java.util.ArrayList;
import java.util.List;

public class DNSSystemTest {
    private final Resolver resolver;
    private final List<TesteResultado> resultados;

    public DNSSystemTest() {
        this.resolver = new Resolver("localhost", 12345);
        this.resultados = new ArrayList<>();
        LogManager.getInstance().info("Iniciando suite de testes do sistema DNS");
    }

    private record TesteResultado(String descricao, boolean sucesso, String mensagemErro) {
    }

    private interface TesteExecucao {
        void executar() throws Exception;
    }

    private void executarTeste(String descricao, TesteExecucao teste) {
        try {
            teste.executar();
            System.out.println("✅ " + descricao + ": OK");
            resultados.add(new TesteResultado(descricao, true, null));
        } catch (AssertionError | Exception e) {
            System.out.println("❌ " + descricao + ": FALHA - " + e.getMessage());
            resultados.add(new TesteResultado(descricao, false, e.getMessage()));
        }
    }

    private void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    public void executarTodosTestes() {
        System.out.println("\n=== Iniciando Testes do Sistema DNS ===\n");

        executarTeste("Registro de novo host", () -> {
            String resultado = resolver.register("testhost2", "192.168.1.100"); // mude aqui para testar
            verificar(resultado.equals("Registrado"),
                    "Esperado 'Registrado', obtido '" + resultado + "'");
        });

        executarTeste("Consulta de host registrado", () -> {
            String ip = resolver.lookup("testhost1");
            verificar(ip.equals("192.168.1.100"), // mude aqui para testar
                    "IP incorreto retornado");
        });

        // Teste de registro duplicado (mesmo hostname)
        executarTeste("Registro duplicado", () -> {
            String resultado = resolver.register("testhost1", "192.168.1.101");
            verificar(resultado.equals("Erro: já existe"),
                    "Registro duplicado deveria falhar");
        });

// Teste de IP duplicado (hostname diferente)
        executarTeste("Registro com IP duplicado", () -> {
            String resultado = resolver.register("testhost2", "192.168.1.100");
            verificar(resultado.equals("Erro: já existe"),
                    "IP duplicado deveria falhar");
        });

        executarTeste("Consulta de host inexistente", () -> {
            String resultado = resolver.lookup("hostinexistente");
            verificar(resultado.equals("Não encontrado"),
                    "Host inexistente deveria retornar 'Não encontrado'");
        });

        mostrarRelatorioFinal();
    }

    private void mostrarRelatorioFinal() {
        System.out.println("\n=== Relatório Final ===");

        long testesPassados = resultados.stream()
                .filter(r -> r.sucesso)
                .count();

        System.out.println("Total de testes: " + resultados.size());
        System.out.println("Testes passados: " + testesPassados);
        System.out.println("Testes falhos: " + (resultados.size() - testesPassados));

        if (resultados.size() - testesPassados > 0) {
            System.out.println("\nDetalhes das falhas:");
            resultados.stream()
                    .filter(r -> !r.sucesso)
                    .forEach(r -> System.out.println("- " + r.descricao + ": " + r.mensagemErro));
        }

        double taxaSucesso = (testesPassados * 100.0) / resultados.size();
        System.out.printf("\nTaxa de sucesso: %.2f%%\n", taxaSucesso);
    }

    public static void main(String[] args) {
        DNSSystemTest tester = new DNSSystemTest();
        tester.executarTodosTestes();
    }
}