package Paralelismo.Estadocompartilhado;

import core.Calcular;
import core.GerarMatriz;
import Implementacaosequencial.Sequencial;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class ParalelismoAtomico {

    public static double processar(double[][] matriz, int tarefas) throws InterruptedException {

        int linhas = matriz.length;
        int tamanhoBloco = linhas / tarefas;


        DoubleAdder somaTotal = new DoubleAdder();
        AtomicInteger tarefasConcluidas = new AtomicInteger(0);

        try (var scope = StructuredTaskScope.open()) {


            for (int t = 0; t < tarefas; t++) {

                int inicio = t * tamanhoBloco;
                int fim = (t == tarefas - 1) ? linhas : inicio + tamanhoBloco;


                scope.fork(() -> {


                    double parcial = processarBloco(matriz, inicio, fim);


                    somaTotal.add(parcial);
                    tarefasConcluidas.incrementAndGet();
                });
            }


            scope.join();
        }


        if (tarefasConcluidas.get() != tarefas) {
            throw new IllegalStateException("Esperadas " + tarefas
                    + " tarefas, mas apenas " + tarefasConcluidas.get() + " registraram resultado.");
        }


        return somaTotal.sum();
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


    public static void main(String[] args) throws InterruptedException {

        double[][] matriz = GerarMatriz.gerarMatriz(500, 500);

        System.out.println("=== Referência sequencial ===");
        double referencia = Sequencial.processar(matriz);

        System.out.println("=== V4a - Estruturado + variável atômica ===");
        System.out.println("Núcleos disponíveis: " + Runtime.getRuntime().availableProcessors());

        for (int tarefas : new int[]{5, 10, 100}) {

            long inicio = System.nanoTime();
            double resultado = processar(matriz, tarefas);
            long fim = System.nanoTime();


            boolean correto = Math.abs(resultado - referencia) <= 1e-9 * Math.abs(referencia);

            System.out.printf("%3d tarefas | tempo: %8.1f ms | resultado: %.6f | correto: %s%n",
                    tarefas, (fim - inicio) / 1_000_000.0, resultado, correto ? "sim" : "NÃO");
        }
    }
}