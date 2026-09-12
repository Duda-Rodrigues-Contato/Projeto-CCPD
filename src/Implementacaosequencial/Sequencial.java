package Implementacaosequencial;
import core.Calcular;
//import core.GerarMatriz;

//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;

public class Sequencial {

    public static double processar(double[][] matriz) {

        //GerarMatriz.gerarMatriz(linha, coluna);

        long inicio = System.nanoTime();
        double resultado = 0.0;

        for (int i = 0; i < matriz.length; i++) {

            for (int j = 0; j < matriz[i].length; j++) {

                resultado += Calcular.calcular(matriz[i][j]);

            }

        }

        long fim = System.nanoTime();

        System.out.println("Tempo: " + (fim - inicio) / 1_000_000.0 + " ms");
        System.out.println("Resultado: " + String.format("%.6f", resultado));
        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println();
        System.out.println();

        return resultado;
    }


}
