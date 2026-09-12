package core;

public class GerarMatriz {

    public static double[][] gerarMatriz(int linhas, int colunas) {

        System.out.println("Criando matriz...");
        double[][] matriz = new double[linhas][colunas];

        System.out.println("Matriz criada.");

        for (int i = 0; i < linhas; i++) {

            for (int j = 0; j < colunas; j++) {

                int valorBase = ((i + 1) * 31 + (j + 1) * 17) % 100;
                matriz[i][j] = (valorBase + 1) / 10000.0;

            }

        }

        return matriz;
    
    }

}
