package Paralelismo.Naoestruturado;

import core.Calcular;
import core.GerarMatriz;
import Implementacaosequencial.Sequencial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class ParalelismoNaoEstruturado {

    public static double processar(double[][] matriz, int tarefas) 
    throws InterruptedException, ExecutionException {

        int linhas = matriz.length;
        int tamanhoBloco = linhas / tarefas;

        double resultado = 0.0;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            List<Future<Double>> futures = new ArrayList<>();
            
            // Disparo assíncrono "não estruturado" das tarefas
            for (int t = 0; t < tarefas; t++) {
                int inicio = t * tamanhoBloco;
                int fim = (t == tarefas - 1) ? linhas : inicio + tamanhoBloco;

                // Submete a tarefa diretamente ao executor sem um escopo delimitador pai/filho
                Future<Double> future = executor.submit(() -> processarBloco(matriz, inicio, fim));
                futures.add(future);

            }

            // Coleta manual e bloqueante dos resultados de cada Future individual
            for (Future<Double> future : futures) {
                resultado += future.get(); // Bloqueia até que o resultado esteja disponível
            }

        } finally {
            // No modelo não estruturado, o encerramento do Executor deve ser garantido manualmente
            executor.shutdown(); // Certifica-se de que o executor seja encerrado após a conclusão das tarefas
        }

        return resultado;
    }

    private static double processarBloco(double[][] matriz, int inicio, int fim) {

        double parcial = 0.0;

        for (int i = inicio; i < fim; i++) {
            
            for (int j = 0; j < matriz[i].length; j++) {
                parcial += Calcular.calcular(matriz[i][j]);
            
            }

        }

        return parcial;

    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        int tamanhoMatriz = 1000;
        int tarefas = 10;

        double[][] matriz = GerarMatriz.gerarMatriz(tamanhoMatriz, tamanhoMatriz);

        System.out.println("=== Referência sequencial ===");
        double referencia = Sequencial.processar(matriz);

        System.out.println("=== V3 - Paralelismo não estruturado ===");
        System.out.println("Núcleos disponíveis: " + Runtime.getRuntime().availableProcessors());

        for (int tarefas1 : new int[]{5, 10, 100}) {

            long inicio = System.nanoTime();   
            double resultado = processar(matriz, tarefas1);
            long fim = System.nanoTime();

            boolean correto = Math.abs(resultado - referencia) <= 1e-9 * Math.abs(referencia);

            System.out.printf("%3d tarefas | tempo: %8.1f ms | resultado: %.6f | correto: %s%n", tarefas1, (fim - inicio) / 1_000_000.0, resultado, correto ? "sim" : "NÃO");

        }

    }
}