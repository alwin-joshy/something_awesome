import java.io.Console;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import org.apache.commons.codec.binary.Base64;


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
                    AccountManager.listAll();
                    break;
                case "v":
                    AccountManager.viewAccount(u);
                    break;
                case "g":
                    System.out.println(StringGenerator.generateString());
                    flag = false; 
                    break;
                case "s":
                    settings(u);
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

    // Settings screen 
    public void settings(User u){
        System.out.println("Enter g to set the default configuration for the random password generator");
        System.out.println("Enter x to erase all records. This cannot be undone.");
        System.out.println("Enter p to change master password");
        System.out.println("Enter f to add a new fingerprint");
        System.out.println("Enter b to return to the main menu.");
        System.out.print("Enter command: ");
        Scanner s = new Scanner(System.in);
        String command = s.nextLine();
        switch (command) {
            case "g":
                StringGenerator.setDefaultInteractive();
                break;
            case "x":
                System.out.print("Are you sure you want to permenantly erase all records? Enter y to confirm and any other button to cancel: ");
                String confirm = s.nextLine();
                if (confirm.equalsIgnoreCase("y")) {
                    SqliteDB.eraseRecords();
                    System.out.println("All records erased.");
                    try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                }
                break;
            case "p":
                changePassword(u);
                break;
            case "f":
                addFingerPrint(u);
            case "b":
                break;
            default:
                System.out.println("Unknown command. Returning to main screen");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                break;
                
        }
    }

    private void addFingerPrint(User u) {

        try {
            ResultSet r = SqliteDB.getUserDetails(u.getUsername());
            String serial = r.getString("serial");
            String salt = r.getString("salt");
            if (serial == null) {
                System.out.println("No arduino associated with this account. Please add an Arduino first");
                return;
            }

            int slot = SqliteDB.getFingerprintSlot();
            System.out.println("You have enrolled " + (slot-1) + "/5 fingerprints");
            if (slot == 6) {
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                return;
            }

            int fID = ArduinoUtil.newFingerprintWrapper(serial, Base64.decodeBase64(salt));
            if (fID == 0) {
                Thread.sleep(2000);
                return; 
            }

            SqliteDB.updateFingerprintSlot(slot, fID);

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
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

    private void changePassword(User u) {
        Common.clearTerminal();
        Common.fancyBanner("Change master password");
        String oldPass = "";
        String salt = "";
    
        try {
            ResultSet res = SqliteDB.getUserDetails(u.getUsername());
            oldPass = res.getString("password");
            salt = res.getString("salt");
        } catch (SQLException e){
            e.printStackTrace();
            System.exit(0);
        }
        byte[] saltArray = Base64.decodeBase64(salt);
        Console c = System.console();
        System.out.print("Enter old password: ");
        if (oldPass.equals(HashUtil.hashPassword(saltArray, c.readPassword()))) {
            char[] newPassArray;
            String newPass1 = "";
            while (true) {
                System.out.print("Enter new password: ");
                newPassArray = c.readPassword();
                newPass1 = HashUtil.hashPassword(saltArray, newPassArray);
                System.out.print("Confirm new password: ");
                if (newPass1.equals(HashUtil.hashPassword(saltArray, c.readPassword()))) break;
                System.out.println("Passwords don't match. Please try again");
            }
            String newKey =  HashUtil.generateEncryptionKey(u.getUsername().toCharArray(), newPassArray, saltArray);
            try {
                reEncryptPasswords(u.getKey(), newKey);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(0);
            } finally {
                SqliteDB.closeConnection();
            }
            u.setKey(newKey);
            SqliteDB.updateMasterPassword(newPass1);
            System.out.println("Password changed successfully!");
       } else {
           System.out.println("Wrong password. Returning to main screen...");
       }

       try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
       
    }
    
    private void reEncryptPasswords(String oldKey, String newKey) throws SQLException {
        RowSetFactory f = RowSetProvider.newFactory();
        CachedRowSet r = f.createCachedRowSet(); // Found this here https://stackoverflow.com/questions/25493837/java-cant-use-resultset-after-connection-close
        r.populate(SqliteDB.allAccountsForUser());
        SqliteDB.closeConnection();
        while (r.next()) {
            String s = r.getString("service");
            String u = r.getString("username");
            System.out.println(s + " " + u);
            String pass = SqliteDB.getAccountPassword(s, u);
            String unencryptedPass = AESUtil.decrypt(pass, oldKey);
            String encryptedPass = AESUtil.encrypt(unencryptedPass, newKey);
            SqliteDB.updatePassword(s, u, encryptedPass);
        }
    }
    
}


