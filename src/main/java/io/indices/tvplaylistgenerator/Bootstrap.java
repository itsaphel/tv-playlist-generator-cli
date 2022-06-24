package io.indices.tvplaylistgenerator;

public class Bootstrap {

    public static void main(String[] args) {
        String name = args[0];
        String identifier = args[1];
        if (name.isEmpty()) {
            printConfigErrorMessage("Args not provided / are invalid");
            System.exit(2);
        }

        new App().run(name, identifier);
    }

    private static void printConfigErrorMessage(String message) {
        System.out.println(message + ". Please read the README for instructions.");
    }
}
