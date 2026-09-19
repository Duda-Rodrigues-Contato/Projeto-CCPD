package Paralelismo.Estadocompartilhado;

import java.util.Queue;
import java.util.concurrent.*;

import Implementacaosequencial.Sequencial;
import core.Calcular;
import core.GerarMatriz;

public class ParalelismoConcorrente {

    public static double processar(double[][] matriz, int tarefas) throws InterruptedException {

        int linhas = matriz.length;
        int tamanhoBloco = linhas / tarefas;

        Queue<Double> resultadosParciais = new ConcurrentLinkedQueue<>(); // Coleção Concorrente para armazenar os
                                                                          // resultados parciais das tarefas

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            for (int i = 0; i < tarefas; i++) {
                int inicio = i * tamanhoBloco;
                int fim = (i == tarefas - 1) ? linhas : inicio + tamanhoBloco;

                executor.submit(() -> {
                    double parcial = processarBloco(matriz, inicio, fim);
                    resultadosParciais.add(parcial); // Adição segura e thread-safe na coleção
                });

            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }

        // Agregação dos valores armazenados na coleção concorrente
        double resultadoTotal = 0.0;
        for (Double parcial : resultadosParciais) {
            resultadoTotal += parcial;
        }

        return resultadoTotal;

    }

    private static double processarBloco(double[][] matriz, int inicio, int fim) {
        double parcial = 0.0;

        for (int t = inicio; t < fim; t++) {

            for (int j = 0; j < matriz[t].length; j++) {

                parcial += Calcular.calcular(matriz[t][j]);

            }
        }

        return parcial;

    }

    public static void main(String[] args) throws InterruptedException {

        int tamanhoMatriz = 1000;
        double[][] matriz = GerarMatriz.gerarMatriz(tamanhoMatriz, tamanhoMatriz);

        System.out.println("=== Referência sequencial ===");
        double referencia = Sequencial.processar(matriz);

        System.out.println("=== V3 - Coleções Concorrentes (ConcurrentLinkedQueue) ===");
        for (int tarefas1 : new int[] { 5, 10, 100 }) {
            long inicio = System.nanoTime();
            double resultado = processar(matriz, tarefas1);
            long fim = System.nanoTime();

            boolean correto = Math.abs(resultado - referencia) <= 1e-9 * Math.abs(referencia);
            System.out.printf("%3d tarefas | tempo: %8.1f ms | resultado: %.6f | correto: %s%n",
                    tarefas1, (fim - inicio) / 1_000_000.0, resultado, correto ? "sim" : "NÃO");
        }

    }

}
