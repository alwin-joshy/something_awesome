import java.io.Console;
import java.security.MessageDigest;
import java.util.Scanner;

public class AccountManager {
 
    public static void addAccount(String uid, StringGenerator sGen)  {
        Common.clearTerminal();
        Common.fancyBanner("Add a new account");
        Scanner s = new Scanner(System.in);
        System.out.print("Enter service (e.g. Google, Facebook, Openlearning): ");
        String service = s.nextLine().toLowerCase();
        System.out.print("Enter username: ");
        String username = s.nextLine();
        if (SqliteDB.checkServiceUsernameExists(uid, service, username)) {
            System.out.println("This account already exists!");
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            return;
        }
        System.out.println("Select from the following options: ");
        System.out.println("1. Generate a random string");
        System.out.println("2. Enter an already selected password");
        String password = ""; 
        while (true){
            int option = s.nextInt();
            s.nextLine();
            boolean confirm = false;
            if (option == 1) {
                while (!confirm) {
                    password = sGen.generateString();
                    System.out.println("The generated password is: " + password);
                    System.out.print("Enter y if this password is satisfactory and any other button to generate a different password: ");
                    String check = s.nextLine().toLowerCase();
                    if (check.equals("y")){
                        confirm = true;
                    }
                }
                break;
            } else if (option == 2) {
                while (!confirm) {
                    Console c =  System.console();
                    System.out.print("Enter password: ");
                    password = c.readPassword().toString();
                    System.out.print("Confirm password: ");
                    if (c.readPassword().toString().equals(password)) {
                        confirm = true;
                    }
                }
                break;
            }
            System.out.println("Please enter a valid option");
        }

        SqliteDB.addAccount(uid, service, username, AESUtil.encrypt(password));

        System.out.println("Success, added new " + service + " account with username " + username);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        return;

    }


    
}
