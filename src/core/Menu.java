package core;

public class Menu {

    public static void exibirMenuPrincipal() {

        System.out.println();
        System.out.println("==========================================");
        System.out.println("      PROJETO DE COMPUTACAO PARALELA");
        System.out.println("==========================================");
        System.out.println("1 - Matriz 500 x 500");
        System.out.println("2 - Matriz 1000 x 1000");
        System.out.println("3 - Matriz 1500 x 1500");
        System.out.println("4 - Matriz 2000 x 2000");
        System.out.println("0 - Exit");
        System.out.println("==========================================");
        System.out.print("Escolha uma opcao: ");
    }

    public static void exibirMenuSecundario() {

        System.out.println();
        System.out.println("==========================================");
        System.out.println("          QUAL O PROCESSO?          ");
        System.out.println("==========================================");
        System.out.println("1 - Sequencial");
        System.out.println("2 - Paralela Nao Estruturada");
        System.out.println("3 - Paralela Estruturada");
        System.out.println("4 - Paralela Atomica");
        System.out.println("5 - Paralela Concorrente");
        System.out.println("0 - Exit");
        System.out.printf("Escolha uma opcao: ");

    }

}
