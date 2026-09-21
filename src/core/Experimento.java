package core;

import Implementacaosequencial.Sequencial;
import Paralelismo.Estadocompartilhado.ParalelismoAtomico;
import Paralelismo.Estadocompartilhado.ParalelismoConcorrente;
import Paralelismo.Estruturado.ParalelismoEstruturado;
import Paralelismo.Naoestruturado.ParalelismoNaoEstruturado;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * ============================================================
 * EXPERIMENTOS (seções 9 e 10 do enunciado)
 * ============================================================
 *
 * Esta classe NÃO é uma nova versão de processamento: ela apenas
 * executa as quatro versões existentes, mede os tempos e calcula
 * as médias e os speedups.
 *
 * Para cada tamanho de matriz:
 *   1. roda o sequencial (referência de tempo e de resultado);
 *   2. roda cada versão paralela com 5, 10 e 100 tarefas;
 *   3. repete cada cenário REPETICOES vezes e calcula a média.
 *
 * Saída:
 *   - console: o andamento e as tabelas finais;
 *   - arquivo resultados/experimentos.csv: uma linha por cenário,
 *     gravada na hora, para nada se perder se a execução parar.
 *
 * ATENÇÃO: a execução completa leva bastante tempo (cerca de uma
 * hora). Para um teste rápido, reduza as constantes abaixo.
 */
public class Experimento {

    private static final int[] TAMANHOS = {500, 1000, 1500, 2000};
    private static final int[] TAREFAS = {5, 10, 100};
    private static final int REPETICOES = 10;

    /*
     * Execuções descartadas antes de medir cada cenário. Servem para
     * o JIT otimizar o código antes da medição. Deixe 0 para medir
     * todas as execuções desde a primeira.
     */
    private static final int AQUECIMENTOS = 0;

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

    /* Uma linha da tabela final. */
    private record Linha(String versao, int tamanho, int tarefas,
                         double tempoMedioMs, double speedup,
                         double resultado, boolean correto) {
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
        System.out.println("Repeticoes por cenario: " + REPETICOES
                + " (aquecimentos descartados: " + AQUECIMENTOS + ")");

        List<Linha> linhas = new ArrayList<>();

        Files.createDirectories(Path.of("resultados"));
        Path arquivo = Path.of("resultados", "experimentos.csv");

        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(arquivo))) {

            csv.println("versao;linhas;colunas;tarefas;repeticoes;tempo_medio_ms;speedup;resultado;resultado_correto");
            csv.flush();

            for (int tamanho : TAMANHOS) {

                System.out.println();
                System.out.println("########## MATRIZ " + tamanho + " x " + tamanho + " ##########");

                double[][] matriz = GerarMatriz.gerarMatriz(tamanho, tamanho);

                // 1. Sequencial: referencia de tempo e de resultado.
                Versao sequencial = (m, t) -> Sequencial.processar(m);
                Linha linhaSequencial = medir("V1 - Sequencial", sequencial, matriz, tamanho, 0, 0.0, null);
                registrar(linhas, csv, linhaSequencial);

                double tempoSequencial = linhaSequencial.tempoMedioMs();
                double referencia = linhaSequencial.resultado();

                // 2. Versoes paralelas, com cada quantidade de tarefas.
                for (int v = 0; v < VERSOES.length; v++) {
                    for (int tarefas : TAREFAS) {
                        Linha linha = medir(NOMES[v], VERSOES[v], matriz, tamanho, tarefas,
                                tempoSequencial, referencia);
                        registrar(linhas, csv, linha);
                    }
                }
            }
        }

        imprimirTabelas(linhas);

        System.out.println();
        System.out.println("Resultados salvos em: " + arquivo.toAbsolutePath());
    }

    /*
     * Executa um cenario REPETICOES vezes e devolve a media.
     * Apenas a chamada de processar entra na medicao.
     */
    private static Linha medir(String nome, Versao versao, double[][] matriz, int tamanho,
                               int tarefas, double tempoSequencial, Double referencia) throws Exception {

        System.out.println();
        System.out.println(tarefas == 0 ? nome : nome + " | " + tarefas + " tarefas");

        for (int a = 0; a < AQUECIMENTOS; a++) {
            versao.processar(matriz, tarefas == 0 ? 1 : tarefas);
        }

        double somaTempos = 0.0;
        double resultado = 0.0;
        boolean correto = true;

        for (int r = 0; r < REPETICOES; r++) {

            long inicio = System.nanoTime();
            resultado = versao.processar(matriz, tarefas == 0 ? 1 : tarefas);
            long fim = System.nanoTime();

            double tempoMs = (fim - inicio) / 1_000_000.0;
            somaTempos += tempoMs;

            if (referencia != null && Math.abs(resultado - referencia) > TOLERANCIA * Math.abs(referencia)) {
                correto = false;
            }

            System.out.printf(PT_BR, "  execucao %2d/%d: %,12.1f ms%n", r + 1, REPETICOES, tempoMs);
        }

        double media = somaTempos / REPETICOES;
        double speedup = referencia == null ? 1.0 : tempoSequencial / media;

        System.out.printf(PT_BR, "  MEDIA: %,.1f ms | speedup: %.2fx | resultado: %.6f | correto: %s%n",
                media, speedup, resultado, correto ? "sim" : "NAO");

        return new Linha(nome, tamanho, tarefas, media, speedup, resultado, correto);
    }

    private static void registrar(List<Linha> linhas, PrintWriter csv, Linha linha) {

        linhas.add(linha);

        csv.println(String.format(PT_BR, "%s;%d;%d;%s;%d;%.3f;%.3f;%.10f;%s",
                linha.versao(), linha.tamanho(), linha.tamanho(),
                linha.tarefas() == 0 ? "-" : String.valueOf(linha.tarefas()),
                REPETICOES, linha.tempoMedioMs(), linha.speedup(),
                linha.resultado(), linha.correto() ? "sim" : "nao"));
        csv.flush();
    }

    /*
     * Imprime, para cada tamanho, a tabela da secao 10 (melhor
     * quantidade de tarefas por implementacao) e o detalhamento por
     * quantidade de tarefas.
     */
    private static void imprimirTabelas(List<Linha> linhas) {

        for (int tamanho : TAMANHOS) {

            System.out.println();
            System.out.println("=== MATRIZ " + tamanho + " x " + tamanho + " ===");
            System.out.println();
            System.out.println("| Implementacao | Tempo medio (ms) | Speedup | Tarefas | Resultado correto |");
            System.out.println("|---|---|---|---|---|");

            Linha sequencial = melhor(linhas, "V1 - Sequencial", tamanho);
            if (sequencial != null) {
                System.out.printf(PT_BR, "| %s | %,.1f | %.2f | - | %s |%n",
                        sequencial.versao(), sequencial.tempoMedioMs(), sequencial.speedup(),
                        sequencial.correto() ? "Sim" : "Nao");
            }

            for (String nome : NOMES) {
                Linha linha = melhor(linhas, nome, tamanho);
                if (linha != null) {
                    System.out.printf(PT_BR, "| %s | %,.1f | %.2f | %d | %s |%n",
                            linha.versao(), linha.tempoMedioMs(), linha.speedup(),
                            linha.tarefas(), linha.correto() ? "Sim" : "Nao");
                }
            }

            System.out.println();
            System.out.println("Tempo medio por quantidade de tarefas:");
            System.out.println();
            System.out.print("| Implementacao |");
            for (int tarefas : TAREFAS) {
                System.out.print(" " + tarefas + " tarefas |");
            }
            System.out.println();
            System.out.print("|---|");
            for (int i = 0; i < TAREFAS.length; i++) {
                System.out.print("---|");
            }
            System.out.println();

            for (String nome : NOMES) {
                System.out.print("| " + nome + " |");
                for (int tarefas : TAREFAS) {
                    Linha linha = buscar(linhas, nome, tamanho, tarefas);
                    if (linha == null) {
                        System.out.print(" - |");
                    } else {
                        System.out.printf(PT_BR, " %,.1f ms (%.2fx) |", linha.tempoMedioMs(), linha.speedup());
                    }
                }
                System.out.println();
            }
        }
    }

    /* Melhor cenario (menor tempo medio) de uma versao num tamanho. */
    private static Linha melhor(List<Linha> linhas, String nome, int tamanho) {

        Linha melhor = null;

        for (Linha linha : linhas) {
            if (linha.versao().equals(nome) && linha.tamanho() == tamanho) {
                if (melhor == null || linha.tempoMedioMs() < melhor.tempoMedioMs()) {
                    melhor = linha;
                }
            }
        }

        return melhor;
    }

    private static Linha buscar(List<Linha> linhas, String nome, int tamanho, int tarefas) {

        for (Linha linha : linhas) {
            if (linha.versao().equals(nome) && linha.tamanho() == tamanho && linha.tarefas() == tarefas) {
                return linha;
            }
        }

        return null;
    }
}