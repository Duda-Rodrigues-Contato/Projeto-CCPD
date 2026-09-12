package core;

import Implementacaosequencial.Sequencial;
//import Paralelismo.Estruturado.ParalelismoEstruturado;
//import Paralelismo.Naoestruturado.ParalelismoNaoEstruturado;
//import Paralelismo.Estadocompartilhado.ParalelismoAtomico;
//import Paralelismo.Estadocompartilhado.ParalelismoConcorrente;

import java.util.Scanner;

public class Main {

        private static void executarProcessamento(int linhas, int colunas) {

                System.out.println();
                System.out.println("==========================================");
                System.out.println("       PROCESSAMENTO SEQUENCIAL");
                System.out.println("==========================================");

                System.out.println(
                                "Matriz: "
                                                + linhas
                                                + " x "
                                                + colunas);

                long quantidadeElementos = (long) linhas * colunas;

                System.out.println(
                                "Elementos: "
                                                + quantidadeElementos);

                System.out.println("Gerando matriz...");

                double[][] matriz = GerarMatriz.gerarMatriz(linhas, colunas);

                System.out.println("Matriz criada.");

                /*
                 * ========================================================
                 * INÍCIO DA MEDIÇÃO
                 * ========================================================
                 *
                 * A criação da matriz não entra na medição.
                 *
                 * Queremos medir apenas o algoritmo de processamento.
                 */
                long inicio = System.nanoTime();

                /*
                 * Executa a versão sequencial.
                 */
                double resultado = Sequencial.processar(matriz);

                /*
                 * ========================================================
                 * FIM DA MEDIÇÃO
                 * ========================================================
                 */
                long fim = System.nanoTime();

                long tempoNano = fim - inicio;

                double tempoMs = tempoNano / 1_000_000.0;

                double tempoSegundos = tempoNano / 1_000_000_000.0;

                /*
                 * Exibe os resultados.
                 */
                System.out.println();
                System.out.println("------------------------------------------");

                System.out.printf(
                                "Resultado: %.6f%n",
                                resultado);

                System.out.printf(
                                "Tempo: %.3f ms%n",
                                tempoMs);

                System.out.printf(
                                "Tempo: %.3f segundos%n",
                                tempoSegundos);

                System.out.println("------------------------------------------");
                System.out.println();
        }

        public static void main(String[] args) {

                Scanner scanner = new Scanner(System.in);

                int opcao = 0;
                int opcao2 = 0;

                /*
                 * Loop principal do programa.
                 */
                do {

                        /*
                         * Exibe o menu.
                         */

                        try {
                                Thread.sleep(1000);
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return; // Stop the program if the thread is interrupted.
                        }
                        Menu.exibirMenuPrincipal();

                        /*
                         * Lê a opção escolhida.
                         */
                        opcao = scanner.nextInt();

                        /*
                         * Seleciona o cenário de execução.
                         */
                        switch (opcao) {

                                case 1:

                                        /*
                                         * 500 x 500
                                         *
                                         * 250.000 elementos.
                                         */

                                        Menu.exibirMenuSecundario();

                                        opcao2 = scanner.nextInt();

                                        switch (opcao2) {
                                                case 1:
                                                        // Executar processo sequencial
                                                        System.out.println();
                                                        System.out.println("==========================================");
                                                        System.out.println("       PROCESSAMENTO SEQUENCIAL");
                                                        System.out.println("==========================================");

                                                        Sequencial.processar(GerarMatriz.gerarMatriz(500, 500));
                                                        break;

                                                case 2:
                                                        // Executar processo paralela não estruturada
                                                        System.out.println();
                                                        System.out.println("==========================================");
                                                        System.out.println("       PROCESSAMENTO PARALELO ESTRUTURADO ");
                                                        System.out.println("==========================================");
                                                        break;
                                                case 3:
                                                        // Executar processo paralela estruturada
                                                        System.out.println();
                                                        System.out.println("==========================================");
                                                        System.out.println("       PROCESSAMENTO PARALELO NÃOESTRUTURADO ");
                                                        System.out.println("==========================================");
                                                        break;
                                                case 4:
                                                        // Executar processo paralela atômica
                                                        System.out.println();
                                                        System.out.println("==========================================");
                                                        System.out.println("       PROCESSAMENTO PARALELO ATÔMICO ");
                                                        System.out.println("==========================================");
                                                        break;
                                                case 5:
                                                        // Executar processo paralela concorrente
                                                        System.out.println();
                                                        System.out.println("==========================================");
                                                        System.out.println("       PROCESSAMENTO PARALELO CONCORRENTE ");
                                                        System.out.println("==========================================");
                                                        break;
                                                default:
                                                        System.out.println("Opção inválida!");
                                        }

                                        break;

                                case 2:

                                        /*
                                         * 1000 x 1000
                                         *
                                         * 1.000.000 elementos.
                                         */
                                        Menu.exibirMenuSecundario();

                                        opcao2 = scanner.nextInt();

                                        switch (opcao2) {
                                                case 1:
                                                        // Executar processo sequencial
                                                        Sequencial.processar(GerarMatriz.gerarMatriz(1000, 1000));
                                                        break;
                                                case 2:
                                                        // Executar processo paralela não estruturada
                                                        break;
                                                case 3:
                                                        // Executar processo paralela estruturada
                                                        break;
                                                case 4:
                                                        // Executar processo paralela atômica
                                                        break;
                                                case 5:
                                                        // Executar processo paralela concorrente
                                                        break;
                                                case 0:
                                                        System.out.println();
                                                        System.out.println(
                                                                        "Encerrando o programa...");

                                                        break;
                                                default:
                                                        System.out.println("Opção inválida!");
                                        }

                                        break;

                                case 3:

                                        /*
                                         * 1500 x 1500
                                         *
                                         * 2.250.000 elementos.
                                         */

                                        Menu.exibirMenuSecundario();

                                        opcao2 = scanner.nextInt();

                                        switch (opcao2) {
                                                case 1:
                                                        // Executar processo sequencial
                                                        Sequencial.processar(GerarMatriz.gerarMatriz(1500, 1500));
                                                        break;
                                                case 2:
                                                        // Executar processo paralela não estruturada
                                                        break;
                                                case 3:
                                                        // Executar processo paralela estruturada
                                                        break;
                                                case 4:
                                                        // Executar processo paralela atômica
                                                        break;
                                                case 5:
                                                        // Executar processo paralela concorrente
                                                        break;
                                                case 0:
                                                        System.out.println();
                                                        System.out.println(
                                                                        "Encerrando o programa...");

                                                        break;
                                                default:
                                                        System.out.println("Opção inválida!");
                                        }

                                        break;

                                case 4:

                                        /*
                                         * 2000 x 2000
                                         *
                                         * 4.000.000 elementos.
                                         */

                                        Menu.exibirMenuSecundario();

                                        opcao2 = scanner.nextInt();

                                        switch (opcao2) {
                                                case 1:
                                                        // Executar processo sequencial
                                                        Sequencial.processar(GerarMatriz.gerarMatriz(2000, 2000));
                                                        break;
                                                case 2:
                                                        // Executar processo paralela não estruturada
                                                        break;
                                                case 3:
                                                        // Executar processo paralela estruturada
                                                        break;
                                                case 4:
                                                        // Executar processo paralela atômica
                                                        break;
                                                case 5:
                                                        // Executar processo paralela concorrente
                                                        break;
                                                case 0:
                                                        System.out.println();
                                                        System.out.println(
                                                                        "Encerrando o programa...");
                                                        break;
                                                default:
                                                        System.out.println("Opção inválida!");
                                        }

                                        break;

                                case 0:

                                        System.out.println();
                                        System.out.println(
                                                        "Encerrando o programa...");

                                        break;

                                default:

                                        System.out.println();
                                        System.out.println(
                                                        "Opção inválida!");

                                        break;
                        }

                        /*
                         * Continua executando enquanto a opção não for 0.
                         */
                } while (opcao != 0);

                scanner.close();

                System.out.println("Programa encerrado.");

        }

}