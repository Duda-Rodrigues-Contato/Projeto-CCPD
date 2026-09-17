package Paralelismo.Estruturado;

import core.Calcular;
import core.GerarMatriz;
import Implementacaosequencial.Sequencial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

/*
 * ============================================================
 * V3 — PARALELISMO ESTRUTURADO
 * ============================================================
 *
 * Mesmo problema da V1/V2, agora com StructuredTaskScope (aula 4).
 *
 *   Método principal
 *         |
 *         v
 *   +-------------------------+
 *   |  StructuredTaskScope    |
 *   |    Subtask 1 ... n      |
 *   |    join()               |
 *   +-------------------------+
 *         |
 *         v
 *     Resultado
 *
 * Recurso preview: exige JDK 26 com language level "26 (Preview)".
 */
public class ParalelismoEstruturado {

    public static double processar(double[][] matriz, int tarefas) throws InterruptedException {

        int linhas = matriz.length;
        int tamanhoBloco = linhas / tarefas;

        double resultado = 0.0;

        /*
         * O escopo é aberto no try-with-resources.
         * open() sem argumentos: espera todas as subtarefas terminarem
         * com sucesso; se alguma falhar, as outras são canceladas e o
         * join() lança StructuredTaskScope.FailedException.
         */
        try (var scope = StructuredTaskScope.open()) {

            List<Subtask<Double>> subtarefas = new ArrayList<>();

            // 1. Divide a matriz por linhas e cria uma subtarefa por bloco.
            for (int t = 0; t < tarefas; t++) {

                int inicio = t * tamanhoBloco;

                // A última tarefa pega as linhas que sobrarem da divisão.
                int fim = (t == tarefas - 1) ? linhas : inicio + tamanhoBloco;

                subtarefas.add(scope.fork(() -> processarBloco(matriz, inicio, fim)));
            }

            // 2. Espera TODAS as subtarefas terminarem.
            scope.join();

            // 3. Combina os resultados parciais (só é permitido depois do join).
            for (Subtask<Double> subtarefa : subtarefas) {
                resultado += subtarefa.get();
            }

        } // 4. O escopo é fechado aqui: nenhuma subtarefa sobrevive ao bloco.

        return resultado;
    }

    /*
     * Soma os valores de um bloco de linhas [inicio, fim).
     * Usa apenas a variável local "parcial": cada subtarefa tem a sua,
     * então não há condição de corrida e não precisa de sincronização.
     */
    private static double processarBloco(double[][] matriz, int inicio, int fim) {

        double parcial = 0.0;

        for (int i = inicio; i < fim; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                parcial += Calcular.calcular(matriz[i][j]);
            }
        }

        return parcial;
    }

    /*
     * ============================================================
     * TESTE PROVISÓRIO
     * ============================================================
     * Compara a V3 com o sequencial. Pode ser removido depois.
     * (O Sequencial imprime o próprio tempo e resultado antes.)
     */
    public static void main(String[] args) throws InterruptedException {

        double[][] matriz = GerarMatriz.gerarMatriz(500, 500);

        System.out.println("=== Referência sequencial ===");
        double referencia = Sequencial.processar(matriz);

        System.out.println("=== V3 - Paralelismo estruturado ===");
        System.out.println("Núcleos disponíveis: " + Runtime.getRuntime().availableProcessors());

        for (int tarefas : new int[]{5, 10, 100}) {

            long inicio = System.nanoTime();
            double resultado = processar(matriz, tarefas);
            long fim = System.nanoTime();

            // Tolerância: somar em blocos muda só as últimas casas decimais.
            boolean correto = Math.abs(resultado - referencia) <= 1e-9 * Math.abs(referencia);

            System.out.printf("%3d tarefas | tempo: %8.1f ms | resultado: %.6f | correto: %s%n",
                    tarefas, (fim - inicio) / 1_000_000.0, resultado, correto ? "sim" : "NÃO");
        }
    }
}