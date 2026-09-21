package Paralelismo.Estadocompartilhado;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.StructuredTaskScope;

import Implementacaosequencial.Sequencial;
import core.Calcular;
import core.GerarMatriz;

public class ParalelismoConcorrente {

    public static double processar(double[][] matriz, int tarefas) throws InterruptedException {

        int linhas = matriz.length;
        int tamanhoBloco = linhas / tarefas;

        Queue<Double> resultadosParciais = new ConcurrentLinkedQueue<>();


        try (var scope = StructuredTaskScope.open()) {

            for (int i = 0; i < tarefas; i++) {
                int inicio = i * tamanhoBloco;
                int fim = (i == tarefas - 1) ? linhas : inicio + tamanhoBloco;

                scope.fork(() -> {
                    double parcial = processarBloco(matriz, inicio, fim);
                    resultadosParciais.add(parcial);
                });

            }


            scope.join();

        }


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

        System.out.println("=== V4b - Estruturado + Colecoes Concorrentes (ConcurrentLinkedQueue) ===");
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