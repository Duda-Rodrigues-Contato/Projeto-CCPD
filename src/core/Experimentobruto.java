package core;

import Implementacaosequencial.Sequencial;
import Paralelismo.Estadocompartilhado.ParalelismoAtomico;
import Paralelismo.Estadocompartilhado.ParalelismoConcorrente;
import Paralelismo.Estruturado.ParalelismoEstruturado;
import Paralelismo.Naoestruturado.ParalelismoNaoEstruturado;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/*
 * ============================================================
 * EXPERIMENTOS (seções 9 e 10 do enunciado)
 * ============================================================
 *
 * Esta classe NÃO é uma nova versão de processamento: ela apenas
 * executa as quatro versões existentes e registra o tempo REAL de
 * cada execução, sem calcular médias.
 *
 * Para cada tamanho de matriz:
 *   1. roda o sequencial (referência de resultado);
 *   2. roda cada versão paralela com 5, 10 e 100 tarefas;
 *   3. repete cada cenário REPETICOES vezes.
 *
 * Saída:
 *   - console: o tempo de cada execução, na hora em que termina;
 *   - arquivo resultados/experimentos_brutos.csv: UMA LINHA POR
 *     EXECUÇÃO, gravada na hora, para nada se perder se parar.
 *
 * ATENÇÃO: a execução completa leva bastante tempo (cerca de uma
 * hora). Para um teste rápido, reduza as constantes abaixo.
 */
public class Experimentobruto {

    private static final int[] TAMANHOS = {500, 1000, 1500, 2000};
    private static final int[] TAREFAS = {5, 10, 100};
    private static final int REPETICOES = 10;

    /*
     * Tolerância para comparar o resultado paralelo com o sequencial.
     * Não se usa "==" porque somar em blocos muda a ordem das somas e
     * altera as últimas casas decimais do double.
     */
    private static final double TOLERANCIA = 1e-9;

    private static final Locale PT_BR = Locale.of("pt", "BR");

    /* Assinatura comum das quatro versões. */
    private interface Versao {
        double processar(double[][] matriz, int tarefas) throws Exception;
    }

    private static final String[] NOMES = {
            "V2 - Paralelismo nao estruturado",
            "V3 - Paralelismo estruturado",
            "V4a - Estruturado + variavel atomica",
            "V4b - Estruturado + colecao concorrente"
    };

    private static final Versao[] VERSOES = {
            ParalelismoNaoEstruturado::processar,
            ParalelismoEstruturado::processar,
            ParalelismoAtomico::processar,
            ParalelismoConcorrente::processar
    };

    public static void main(String[] args) throws Exception {

        System.out.println("==========================================");
        System.out.println("            EXPERIMENTOS");
        System.out.println("==========================================");
        System.out.println("Java " + Runtime.version());
        System.out.println("Nucleos logicos: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Execucoes por cenario: " + REPETICOES);

        Files.createDirectories(Path.of("resultados"));
        Path arquivo = Path.of("resultados", "experimentos_brutos.csv");

        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(arquivo))) {

            csv.println("versao;linhas;colunas;tarefas;execucao;tempo_ms;resultado;resultado_correto");
            csv.flush();

            for (int tamanho : TAMANHOS) {

                System.out.println();
                System.out.println("########## MATRIZ " + tamanho + " x " + tamanho + " ##########");

                double[][] matriz = GerarMatriz.gerarMatriz(tamanho, tamanho);

                // 1. Sequencial. O resultado da primeira execução é a referência.
                Versao sequencial = (m, t) -> Sequencial.processar(m);
                double referencia = executar("V1 - Sequencial", sequencial, matriz, tamanho, 0, null, csv);

                // 2. Versões paralelas, com cada quantidade de tarefas.
                for (int v = 0; v < VERSOES.length; v++) {
                    for (int tarefas : TAREFAS) {
                        executar(NOMES[v], VERSOES[v], matriz, tamanho, tarefas, referencia, csv);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("Resultados salvos em: " + arquivo.toAbsolutePath());
    }

    /*
     * Executa um cenário REPETICOES vezes e grava cada execução no CSV.
     * Apenas a chamada de processar entra na medição.
     * Devolve o resultado calculado (usado como referência no sequencial).
     */
    private static double executar(String nome, Versao versao, double[][] matriz, int tamanho,
                                   int tarefas, Double referencia, PrintWriter csv) throws Exception {

        System.out.println();
        System.out.println(tarefas == 0 ? nome : nome + " | " + tarefas + " tarefas");

        double resultado = 0.0;

        for (int r = 1; r <= REPETICOES; r++) {

            long inicio = System.nanoTime();
            resultado = versao.processar(matriz, tarefas == 0 ? 1 : tarefas);
            long fim = System.nanoTime();

            double tempoMs = (fim - inicio) / 1_000_000.0;

            boolean correto = referencia == null
                    || Math.abs(resultado - referencia) <= TOLERANCIA * Math.abs(referencia);

            System.out.printf(PT_BR, "  execucao %2d/%d: %,12.1f ms | correto: %s%n",
                    r, REPETICOES, tempoMs, correto ? "sim" : "NAO");

            csv.println(String.format(PT_BR, "%s;%d;%d;%s;%d;%.3f;%.10f;%s",
                    nome, tamanho, tamanho,
                    tarefas == 0 ? "-" : String.valueOf(tarefas),
                    r, tempoMs, resultado, correto ? "sim" : "nao"));
            csv.flush();
        }

        return resultado;
    }
}