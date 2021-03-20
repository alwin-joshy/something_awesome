import java.util.ArrayList;
import java.util.Scanner;

public class MainScreen {

    private StringGenerator sGen;

    public MainScreen() {
        sGen = new StringGenerator();
    }

    public void begin(User u) {
        Common.clearTerminal();
        Common.fancyBanner("Hi " + u.getUsername() + "! Press ? to view commands");
        System.out.print("Enter command: ");
        Scanner s = new Scanner(System.in);
        String command = s.nextLine();
        while (!command.equals("q")) {
            switch (command) {
                case "?":
                    printCommands();
                    break;
                case "a": 
                    AccountManager.addAccount(u.getuid(), sGen);
                    Common.clearTerminal();
                    Common.fancyBanner("Hi " + u.getUsername() + "! Press ? to view commands");
                    break;
                case "c":
                    //changeAccount();
                case "v":
                    //viewAccounts();
                case "p":
                    //fetchCredentials();
                case "g":
                    System.out.println(sGen.generateString());
                    break;
                case "s":
                    settings();
            }

            System.out.print("Enter command: ");
            command = s.nextLine();
        }

        System.out.println("Enter x to quit and any other button to return to the login screen");
        System.out.print("Enter command: ");
        command = s.nextLine();

        if (command.equals("x")) {
            System.exit(1);
        }
    }

    public void printCommands() {
        System.out.println("a - add new account\n"+
                           "c - change account details\n"+
                           "v - view all accounts\n"+
                           "p - search for account credentials\n"+
                           "g - generate random string\n"+
                           "s - settings\n"+
                           "q - logout\n");
    }
}
