import java.io.Console;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AccountManager {
    
    // Used for adding an account to the password manager
    public static void addAccount(User u)  {
        Common.clearTerminal();
        Common.fancyBanner("Add a new account");
        Scanner s = new Scanner(System.in);
        System.out.print("Enter service (e.g. Google, Facebook, Openlearning): ");
        String service = s.nextLine().toLowerCase();
        System.out.print("Enter username: ");
        String username = s.nextLine();
        String userEncrypted = AESUtil.encrypt(username, u.getKey());
        if (checkServiceUsernameExists(service, username, u)) { // If the account already exists, we don't want to overwrite it 
            System.out.println("This account already exists!");
            try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            return;
        }
       
        String password = getPassword(u);
        SqliteDB.addAccount(service, userEncrypted, password); // Adds the account to the database

        System.out.println("Success, added new " + service + " account with username " + username);
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        return;
    }

    private static boolean checkServiceUsernameExists(String service, String userToCheck, User u) {
        ResultSet r = SqliteDB.getAccountsFromService(service);
        try {
            while (r.next()) {
                String userChecking = r.getString("username");
                if (userToCheck.equals(AESUtil.decrypt(userChecking, u.getKey()))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqliteDB.closeConnection();
        }

        return false;

    }

    // To modify an existing account
    public static void modifyAccount(User u) {
        Common.clearTerminal();
        Common.fancyBanner("Modify an existing account");
        ArrayList<String> selected = selectAccount(u);
        if (selected == null) return;
        System.out.println("You have selected " + AESUtil.decrypt(selected.get(1), u.getKey()) +". What would you like to do?");
        System.out.println("1. Change the password");
        System.out.println("2. Delete this account");
        System.out.println("Other. Return to main menu");
        System.out.println();
        System.out.print("Enter option: ");
        Scanner s = new Scanner(System.in);
        String option = s.nextLine();
        if (option.equals("1")) { // Changes the password and updates the database
            String password = getPassword(u);
            if (SqliteDB.updatePassword(selected.get(0), selected.get(1), password) == 1){
                System.out.println("Password changed successfully");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            }
        } else if (option.equals("2")) { // Removes the account from the database
            System.out.print("Are you sure you want to delete this account? Details will be lost permanently and cannot be recovered. Enter y to confirm and anything else to cancel: ");
            option = s.nextLine();
            if (option.equalsIgnoreCase("y")){
                SqliteDB.deleteAccount(selected.get(0), selected.get(1));
                System.out.println(selected.get(1) + " successfully deleted");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
            } 
        }

        return;

    }
    
    // Prints out the password of a selected account
    public static void viewAccount(User u) {
        Common.clearTerminal();
        Common.fancyBanner("View account details");
        ArrayList<String> selected = selectAccount(u);
        if (selected == null) return;
        String encryptedPass = SqliteDB.getAccountPassword(selected.get(0), selected.get(1));
        System.out.println("Service: " + selected.get(0));
        System.out.println("Username: " + AESUtil.decrypt(selected.get(1), u.getKey()));
        System.out.println("Password: " + AESUtil.decrypt(encryptedPass, u.getKey()));
        System.out.print("\nEnter any character to return to the main menu: ");
        Scanner s = new Scanner(System.in);
        s.nextLine();
    }

    // List of all services and accounts 
    public static void listAll(User u){
        Common.clearTerminal();
        Common.fancyBanner("List of all registered accounts");
        ResultSet res = SqliteDB.allAccountsForUser();
        try {
            res.next();
            String prev_service = null;
            String curr_service;
            int i = 1;
            do {
                curr_service = res.getString("service");
                if (!curr_service.equals(prev_service)){
                    i = 1;
                    System.out.println(curr_service);
                    prev_service = curr_service;
                }
                System.out.println("    " + i + ". " + AESUtil.decrypt(res.getString("username"), u.getKey()));
                i++;
            } while (res.next());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqliteDB.closeConnection();
        }
        System.out.print("\nEnter any character to return to the main menu: ");
        Scanner s = new Scanner(System.in);
        s.nextLine();
    }

    // Reusable code to allow user to enter/generate and confirm a password 
    private static String getPassword(User u) {
        Scanner s = new Scanner(System.in);
        System.out.println("Select from the following options: ");
        System.out.println("1. Generate a random string");
        System.out.println("2. Enter an already selected password");
        System.out.println("Enter selection: ");
        String password = ""; 
        while (true){
            int option = s.nextInt();
            s.nextLine();
            boolean confirm = false;
            if (option == 1) {
                while (!confirm) {
                    password = StringGenerator.generateString();
                    System.out.println("\nThe generated password is: " + password);
                    System.out.print("Press enter if this password is satisfactory and any other key to generate a different password: ");
                    String check = s.nextLine().toLowerCase();
                    if (check.equals("")){
                        confirm = true;
                    }
                }
                break;
            } else if (option == 2) {
                while (!confirm) {
                    Console c =  System.console();
                    System.out.print("Enter password: ");
                    password = new String(c.readPassword());
                    if (!Common.checkPassword(password)) {
                        System.out.println("It is recommended that you use a stronger passsword. Would you like to procceed anyway? Press enter to use this password and any other button to re-enter a different password");
                        String check = s.nextLine();
                        if (!check.equals("")) {
                            continue;
                        }
                    }
                    System.out.print("Confirm password: ");
                    String password2 = new String(c.readPassword());
                    if (password.equals(password2)) {
                        confirm = true;
                    }
                }
                break;
            }
            System.out.println("Please enter a valid option");
        }

        return AESUtil.encrypt(password, u.getKey());
    }

    // Reusable code to allow a user to select an account 
    private static ArrayList<String> selectAccount(User u) {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter service e.g. Google, Facebook, Openlearning: ");
        String service = s.nextLine().toLowerCase();
        System.out.println();
        System.out.println("Accounts registered for " + service + ":");
        ResultSet r = SqliteDB.getAccountsFromService(service);
        ArrayList<String> usernames = new ArrayList<String>();
        try {
            if (!r.next()) {
                System.out.println("You do not have any accounts associated with " + service);
                Thread.sleep(2000);
                return null;
            }
            int i = 0;
            do {
                String username = r.getString("username");
                System.out.println(i + ". " + AESUtil.decrypt(username, u.getKey()));
                usernames.add(username);
                i++;
            } while (r.next());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqliteDB.closeConnection();
        }

        int index = 0;
        while (true) {
            System.out.print("Enter the corresponding number of the account you would like to select or -1 to back to the main menu: ");
            index = s.nextInt();
            s.nextLine();
            if (index == -1) {
                return null;
            }
            if (index <= usernames.size()) {
                break;
            }
            System.out.println("Invalid selection! Please choose a number between 0 and " + (usernames.size() - 1) + "or enter -1 to go back to main menu");
        }
        System.out.println();
        return new ArrayList<String>(Arrays.asList(service, usernames.get(index)));
    }

    
}
