import java.util.Scanner;


/*
 * ============================================================
 * MÉTODO calcular
 * ============================================================
 *
 * Realiza um cálculo matemático relativamente pesado.
 *
 * Esse método é chamado uma vez para cada elemento da matriz.
 *
 * A intenção é criar uma carga de processamento suficiente
 * para que posteriormente possamos comparar:
 *
 *   - execução sequencial;
 *   - Threads;
 *   - ExecutorService;
 *   - CompletableFuture;
 *   - paralelismo estruturado;
 *   - outras estratégias de paralelização.
 *
 * IMPORTANTE:
 *
 * O método não possui estado compartilhado.
 *
 * Isso será importante quando criarmos as versões paralelas.
 */
private static double calcular(double valor) {

    double resultado = valor;

    /*
     * Executamos 1000 iterações para cada elemento.
     *
     * Como a matriz possui muitos elementos, esse pequeno
     * processamento individual acaba gerando uma carga
     * computacional significativa.
     */
    for (int i = 0; i < 1000; i++) {

        resultado +=
                Math.sin(valor + i)
                        * Math.cos(valor - i)
                        * Math.sqrt(Math.abs(valor) + 1);
    }

    return resultado;
}


/*
 * ============================================================
 * MÉTODO processar
 * ============================================================
 *
 * Esta é a implementação SEQUENCIAL.
 *
 * O processamento acontece da seguinte maneira:
 *
 *       matriz
 *          |
 *          v
 *      linha 0
 *          |
 *      coluna 0 -> calcular()
 *      coluna 1 -> calcular()
 *      coluna 2 -> calcular()
 *          |
 *          v
 *      linha 1
 *          |
 *          v
 *        ...
 *
 * Apenas depois que um elemento termina de ser processado
 * o próximo elemento é iniciado.
 *
 * Essa implementação será utilizada como BASELINE.
 *
 * Ou seja, as versões paralelas deverão:
 *
 * 1. produzir o mesmo resultado;
 * 2. tentar reduzir o tempo de execução.
 */
private static double processar(double[][] matriz) {

    double resultado = 0.0;

    /*
     * Percorre todas as linhas.
     */
    for (int i = 0; i < matriz.length; i++) {

        /*
         * Percorre todas as colunas da linha atual.
         */
        for (int j = 0; j < matriz[i].length; j++) {

            /*
             * Obtém o valor da posição [i][j]
             * e executa o cálculo.
             */
            resultado += calcular(matriz[i][j]);
        }
    }

    return resultado;
}


/*
 * ============================================================
 * MÉTODO gerarMatriz
 * ============================================================
 *
 * Cria uma matriz com valores DETERMINÍSTICOS.
 *
 * Não utilizamos Random.
 *
 * Isso é MUITO importante para o projeto.
 *
 * Todas as execuções utilizarão exatamente os mesmos dados.
 *
 * Dessa forma:
 *
 *       Sequencial
 *            |
 *            +----> Resultado A
 *
 *       Paralelo
 *            |
 *            +----> Resultado A
 *
 * Se os resultados forem diferentes, provavelmente existe
 * algum problema na implementação paralela.
 */
private static double[][] gerarMatriz(
        int linhas,
        int colunas) {

    double[][] matriz = new double[linhas][colunas];

    /*
     * Preenche a matriz com valores pequenos e positivos.
     *
     * O valor depende exclusivamente da posição [i][j].
     *
     * Portanto, a matriz sempre será igual para uma mesma
     * dimensão.
     *
     * O valor estará entre:
     *
     *       0,0001
     *       e
     *       0,0100
     *
     * Esses valores foram escolhidos para manter o cálculo
     * numericamente seguro e produzir resultados positivos.
     */
    for (int i = 0; i < linhas; i++) {

        for (int j = 0; j < colunas; j++) {

            /*
             * Gera um valor entre 1 e 100.
             *
             * O operador % garante que o valor fique
             * dentro desse intervalo.
             */
            int valorBase =
                    ((i + 1) * 31 + (j + 1) * 17) % 100;

            /*
             * Converte para valores entre:
             *
             * 0,0001 e 0,0100
             */
            matriz[i][j] =
                    (valorBase + 1) / 10000.0;
        }
    }

    return matriz;
}


/*
 * ============================================================
 * MÉTODO executarProcessamento
 * ============================================================
 *
 * Responsável por:
 *
 * 1. criar a matriz;
 * 2. iniciar a medição;
 * 3. executar o processamento;
 * 4. finalizar a medição;
 * 5. apresentar os resultados.
 */
private static void executarProcessamento(
        int linhas,
        int colunas) {

    System.out.println();
    System.out.println("==========================================");
    System.out.println("       PROCESSAMENTO SEQUENCIAL");
    System.out.println("==========================================");

    System.out.println(
            "Matriz: "
                    + linhas
                    + " x "
                    + colunas);

    /*
     * Utilizamos long para evitar overflow na multiplicação
     * quando calculamos a quantidade de elementos.
     */
    long quantidadeElementos =
            (long) linhas * colunas;

    System.out.println(
            "Elementos: "
                    + quantidadeElementos);

    /*
     * Cria a matriz determinística.
     */
    System.out.println("Gerando matriz...");

    double[][] matriz =
            gerarMatriz(linhas, colunas);

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
    double resultado =
            processar(matriz);

    /*
     * ========================================================
     * FIM DA MEDIÇÃO
     * ========================================================
     */
    long fim = System.nanoTime();

    long tempoNano =
            fim - inicio;

    double tempoMs =
            tempoNano / 1_000_000.0;

    double tempoSegundos =
            tempoNano / 1_000_000_000.0;

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


/*
 * ============================================================
 * MÉTODO exibirMenu
 * ============================================================
 *
 * Apresenta as opções disponíveis.
 *
 * Não existe mais a opção de informar um tamanho
 * personalizado.
 *
 * Isso garante que todos os alunos trabalhem com os mesmos
 * cenários de teste.
 */
private static void exibirMenu() {

    System.out.println();
    System.out.println("==========================================");
    System.out.println("      PROJETO DE COMPUTAÇÃO PARALELA");
    System.out.println("==========================================");
    System.out.println("1 - Matriz 500 x 500");
    System.out.println("2 - Matriz 1000 x 1000");
    System.out.println("3 - Matriz 1500 x 1500");
    System.out.println("4 - Matriz 2000 x 2000");
    System.out.println("0 - Exit");
    System.out.println("==========================================");
    System.out.print("Escolha uma opção: ");
}
/*
 * ============================================================
 * MÉTODO main
 * ============================================================
 *
 * Ponto de entrada da aplicação.
 *
 * O menu permanece em execução até que o usuário escolha
 * a opção 0.
 */
public static void main(String[] args) {

    Scanner scanner =
            new Scanner(System.in);

    int opcao;

    /*
     * Loop principal do programa.
     */
    do {

        /*
         * Exibe o menu.
         */
        exibirMenu();

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
                executarProcessamento(
                        500,
                        500);

                break;


            case 2:

                /*
                 * 1000 x 1000
                 *
                 * 1.000.000 elementos.
                 */
                executarProcessamento(
                        1000,
                        1000);

                break;


            case 3:

                /*
                 * 1500 x 1500
                 *
                 * 2.250.000 elementos.
                 */
                executarProcessamento(
                        1500,
                        1500);

                break;


            case 4:

                /*
                 * 2000 x 2000
                 *
                 * 4.000.000 elementos.
                 */
                executarProcessamento(
                        2000,
                        2000);

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

    System.out.println(
            "Programa encerrado.");
}
