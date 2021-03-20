import java.io.Console;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.util.ArrayList;
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
       
        String password = getPassword(sGen);
        SqliteDB.addAccount(uid, service, username, password);

        System.out.println("Success, added new " + service + " account with username " + username);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        return;
    }

    public static void modifyAccount(String uid, StringGenerator sGen) {
        Common.clearTerminal();
        Common.fancyBanner("Modify an existing account");
        Scanner s = new Scanner(System.in);
        System.out.print("Enter service e.g. Google, Facebook, Openlearning: ");
        String service = s.nextLine().toLowerCase();
        System.out.println();
        System.out.println("Accounts registered for " + service + ":");
        ResultSet r = SqliteDB.getAccountsFromService(uid, service);
        ArrayList<String> usernames = new ArrayList<String>();
        try {
            if (!r.next()) {
                System.out.println("You do not have any accounts associated with " + service);
                Thread.sleep(2000);
                return;
            }
            int i = 0;
            do {
                String username = r.getString("username");
                System.out.println(i + ". " + username);
                usernames.add(username);
                i++;
            } while (r.next());

        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean check = false;
        int index = 0;
        while (!check) {
            System.out.print("Enter the corresponding number of the account you would like to change or -1 to back to the main menu: ");
            index = s.nextInt();
            s.nextLine();
            if (index == -1) {
                return;
            }
            if (index <= usernames.size()) {
                break;
            }
            System.out.println("Invalid selection! Please choose a number between 0 and " + (usernames.size() - 1) + "or enter -1 to go back to main menu");
        }
        System.out.println("You have selected " + usernames.get(index) +". What would you like to do?");
        System.out.println("1. Change the password");
        System.out.println("2. Delete this account");
        System.out.println("Other. Return to main menu");
        System.out.println();
        System.out.print("Enter option: ");
        String option = s.nextLine();
        if (option.equals("1")) {
            String password = getPassword(sGen);
            if (SqliteDB.updatePassword(uid, service, usernames.get(index), password) == 1){
                System.out.println("Password changed successfully");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            }
        } else if (option.equals("2")) {
            System.out.print("Are you sure you want to delete this account? Details will be lost permanently and cannot be recovered. Enter y to confirm and anything else to cancel: ");
            option = s.nextLine();
            if (option.equals("y")){
                SqliteDB.deleteAccount(uid, service, usernames.get(index));
                System.out.println(usernames.get(index) + " successfully deleted");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            } 
        }

        return;

    }


    private static String getPassword(StringGenerator sGen) {
        Scanner s = new Scanner(System.in);
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

        return AESUtil.encrypt(password);
    }

    
}
