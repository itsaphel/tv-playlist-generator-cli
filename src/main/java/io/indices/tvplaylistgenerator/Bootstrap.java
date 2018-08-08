package io.indices.tvplaylistgenerator;

public class Bootstrap {

    public static void main(String[] args) {
        String showId = args[0];
        if (showId.isEmpty()) {
            printConfigErrorMessage("Args not provided / are invalid");
            System.exit(2);
        }

        new App().run(showId);
    }

    private static void printConfigErrorMessage(String message) {
        System.out.println(message + ". Please read the README for instructions.");
    }
}
