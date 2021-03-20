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
                    AccountManager.modifyAccount(u.getuid(), sGen);
                    Common.clearTerminal();
                    Common.fancyBanner("Hi " + u.getUsername() + "! Press ? to view commands");
                    break;
                case "v":
                    //viewAccounts();
                case "p":
                    //fetchCredentials();
                case "g":
                    System.out.println(sGen.generateString());
                    break;
                case "s":
                    settings();
                    Common.clearTerminal();
                    Common.fancyBanner("Hi " + u.getUsername() + "! Press ? to view commands");
                    break;
            }
            System.out.print("Enter command: ");
            command = s.nextLine();
        }

        
        System.out.println("Enter x to quit and any other character to return to the login screen");
        System.out.print("Enter command: ");
        command = s.nextLine();

        if (command.equals("x")) {
            System.exit(1);
        }
    }


    public void settings(){
        System.out.println("Enter g to set the default configuration for the random password generator");
        System.out.println("Enter x to erase all records. This cannot be undone.");
        System.out.println("Enter b to return to the main menu.");
        System.out.print("Enter command: ");
        Scanner s = new Scanner(System.in);
        String command = s.nextLine();
        switch (command) {
            case "g":
                sGen.setDefaultInteractive();
                break;
            case "x":
                System.out.print("Are you sure you want to permenantly erase all records? Enter y to confirm and any other button to cancel: ");
                String confirm = s.nextLine();
                if (confirm.equals("y")) {
                    SqliteDB.eraseRecords();
                    System.out.println("All records erased.");
                    try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                }
                break;
            case "b":
                break;
            default:
                System.out.println("Unknown command. Returning to main screen");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                break;
                
        }
    }

    public void printCommands() {
        System.out.println("a - add new account\n"+
                           "c - change account details\n"+
                           "v - view all accounts\n"+
                           "p - search for account credentials\n"+
                           "g - generate random string\n"+
                           "s - settings\n"+
                           "q - logout");
    }
}
