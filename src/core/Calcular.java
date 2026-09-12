package core;

public class Calcular {
    public static double calcular(double valor) {
        
        double resultado = valor;
        
        for (int i = 0; i < 1000; i++) {
            resultado += Math.sin(valor + i)
                    * Math.cos(valor - i)
                    * Math.sqrt(Math.abs(valor) + 1);
        }
        
        return resultado;
    
    }
}
