import java.util.Scanner;

public class MainScreen {

    public void begin(User u) {
        Common.clearTerminal();
        Common.fancyBanner("Hi " + u.getUsername() + "! Press ? to view commands");

        System.out.print("Enter command: ");
        Scanner s = new Scanner(System.in);
        String command = s.nextLine();
        boolean flag = true;
        while (!command.equals("q")) {
            switch (command) {
                case "?":
                    printCommands();
                    flag = false;
                    break;
                case "a": 
                    AccountManager.addAccount(u);
                    break;
                case "m":
                    AccountManager.modifyAccount(u);
                    break;
                case "l":
                    AccountManager.listAll(u);
                    break;
                case "v":
                    AccountManager.viewAccount(u);
                    break;
                case "g":
                    System.out.println(StringGenerator.generateString());
                    flag = false; 
                    break;
                case "s":
                    Settings.showSettings(u);
                    break;
                case "c":
                    break;
                default:
                    System.out.println("Unknown command. Please enter a valid command (press ? to see full list)");
                    flag = false; 
                    break;
            }
            if (flag) {
                Common.clearTerminal();
                Common.fancyBanner("Hi " + u.getUsername() + "! Press ? to view commands");
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

        // Prints out all the commands 
        public void printCommands() {
            System.out.println("a - add new account\n"+
                                "m - modify account details/delete account\n"+
                                "l - view all accounts\n"+
                                "v - view the details for a specific account\n"+
                                "g - generate random string\n"+
                                "s - settings\n"+
                                "c - clear the console\n"+
                                "q - logout");
        }
    


    
}


