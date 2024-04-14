package genericmenu;

import Servidor.services.CurrencyService;
import cliente.UDPClient;
import shd_utils.ParseHelpers;
import shd_utils.Services;

import java.util.Scanner;

public class MenuApp {
    private static final Services[] SERVICES = Services.values();
    private final Scanner sc;

    private final UDPClient client;
    private boolean hasDownloadedCurrencies = false;

    //ONLY FOR TESTING PURPOSES.
    public MenuApp() {
        client = new UDPClient();
        sc = new Scanner(System.in);
    }

    private void displayOptions() {
        System.out.println("/******** MENU OPCIONES ***********/");
        System.out.println("1. Buscar una palabra en el diccionario.");
        System.out.println("2. Agregar una palabra al diccionario.");
        System.out.println("3. Cambiar moneda.");
        System.out.println("4. Cerrar programa.");
    }

    private void prepareWordSearch() {
        System.out.println("Introduzca la palabra a buscar:");
        String word = sc.next().trim();

        if(!ParseHelpers.isValidWord(word)) {
            System.out.println("La palabra tiene que ser mayor a 3 caracteres.");
            return;
        }

        String content = ParseHelpers.createContents(Services.SEARCH_WORD, word);
        String resp = client.sendMessage(content);

        if(resp.equals("NO_DEF")) {
            System.out.printf("La palabra %s no posee significados.", word);
            return;
        }

        System.out.printf("Definiciones de %s\n%s", word, resp);
        System.out.println();
        //System.out.printf("Y los sockets? %s\n", resp);
    }

    private void prepareWordAdding() {
        System.out.println("Introduzca la palabra a colocar significado.");
        String word = sc.nextLine().trim();
        System.out.println("Introduzca el signficado correspondiente.");
        String meaning = sc.nextLine().trim();

        if(!ParseHelpers.isValidWord(word) || !ParseHelpers.isValidWord(meaning)) {
            System.out.println("No se permiten definiciones vacias o palabras muy pequeñas (menores a 3 caracteres).");
            return;
        }

        String content = ParseHelpers.createContents(Services.ADD_MEANING, word, meaning);
        String resp = client.sendMessage(content);

        System.out.println(resp);
    }

    private void prepareCurrencies() {
        String content;

        //revisar las monedas.
        if (!hasDownloadedCurrencies) {
            content = ParseHelpers.createContents(Services.CHANGE_CURRENCY, CurrencyService.AVAILABLE_COMMAND);
            String currencies = client.sendMessage(content);
            System.out.println(currencies);
            hasDownloadedCurrencies = true;
        }

        System.out.println("Introduzca la moneda (las mostradas en pantalla):");
        System.out.println("Para ser convertida a CLP.");
        String type = sc.nextLine();
        System.out.printf("Introduzca el monto de esa moneda (Moneda escogida: %s)\n", type);
        int amount = sc.nextInt();

        if(amount < 0){
            System.out.println("El monto no puede ser menor a 0.");
            return;
        }

        content = ParseHelpers.createContents(Services.CHANGE_CURRENCY, type, Integer.toString(amount));
        String resp = client.sendMessage(content);

        System.out.println(resp);
    }

    //you can't return onto while true without breaking it.
    //returns true to stop.
    private boolean doOptions() {
        int input = sc.nextInt();
        // System.out.printf("The selected input was: %s\n", input);
        if(input <= 0 || input > SERVICES.length) {
            return false;
        }

        //goofy ahh reset nextline.
        sc.nextLine();

        Services service = SERVICES[input - 1];

        System.out.println("Servicio actual: " + service.name());

        switch (service){
            case SEARCH_WORD:
                prepareWordSearch();
                break;
            case ADD_MEANING:
                prepareWordAdding();
                break;
            case CHANGE_CURRENCY:
                prepareCurrencies();
                break;
            //HACK: Add this for avoiding thinking too much.
            case NULL_SERVICE:
                System.out.println("Saliendo del menu...");
                return true;
        }

        return false;
    }

    public void runMenu() {
        //crear el scanner.
        while(true) {
            displayOptions();
            if (doOptions()){
                sc.close();
                break;
            }
        }
    }
}
