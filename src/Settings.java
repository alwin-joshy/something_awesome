import java.io.Console;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import org.apache.commons.codec.binary.Base64;

public class Settings {

    // Settings screen 
    public static void showSettings(User u){
        System.out.println("Enter g to set the default configuration for the random password generator");
        System.out.println("Enter x to erase all records. This cannot be undone.");
        System.out.println("Enter p to change master password");
        System.out.println("Enter a to set/change device for arduino authentication");
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
                break;
            case "a":
                addArduino(u);
                break;
            case "b":
                break;
            default:
                System.out.println("Unknown command. Returning to main screen");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                break;
                
        }
    }

    private static void addArduino(User u) {
        String serial = "";
        String salt = "";
        try {
            ResultSet r = SqliteDB.getUserDetails(u.getUsername());
            serial = r.getString("serial");
            salt = r.getString("salt");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            SqliteDB.closeConnection();
        }

        // If there is already a connected device
        if (!serial.equals("")) {
            System.out.println("There is already a device associated with this account. Changing this may make your account inaccessible if you have fingerprint authentication enabled. ");
            System.out.print("Are you sure you want to proceed? Enter y to continue or any other button to return to main menu: ");
            Scanner s = new Scanner(System.in);
            String confirm = s.nextLine();
            if (!confirm.equalsIgnoreCase("y")) {
                return;
            }
        }

        serial = ArduinoUtil.addArduino();
        if (serial.equals("")) {
            return;
        }

        String serialHash = HashUtil.hash(Base64.decodeBase64(salt), serial.toCharArray());
        SqliteDB.updateSerial(serialHash);

        System.out.println("Sucessfully added device!");
        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

    }

    private static void addFingerPrint(User u) {

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

    private static void changePassword(User u) {
        Common.clearTerminal();
        Common.fancyBanner("Change master password");
        String oldPass = "";
        String oldSalt = "";
    
        try {
            ResultSet res = SqliteDB.getUserDetails(u.getUsernameHash());
            oldPass = res.getString("password");
            oldSalt = res.getString("salt");
            oldSerialHash = res.getString("serial");
        } catch (SQLException e){
            e.printStackTrace();
            System.exit(0);
        } finally {
            SqliteDB.closeConnection();
        }

        byte[] oldSaltArray = Base64.decodeBase64(oldSalt);

        if (!oldSerialHash.equals("")) {
            SerialPort p = ArduinoUtil.checkConnection(oldSerialHash, oldSaltArray)
            if (p == null) {
                System.out.println("Please connect the Arduino associated with this account and try again.")
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                return;
            }
            String serial = ArduinoUtil.getSerialNumber(p);
            if (serial == null) {
                System.out.println("Unable to read device serial");
                try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
                return;
            }
        }

        
        Console c = System.console();
        System.out.print("Enter old password: ");
        if (oldPass.equals(HashUtil.hash(oldSaltArray, c.readPassword()))) {
            byte[] newSalt = HashUtil.saltGen();
            char[] newPassArray;
            String newPass = "";
            while (true) {
                System.out.print("Enter new password: ");
                newPassArray = c.readPassword();
                //if (!Common.checkPassword(new String(newPassArray))) continue;
                newPass = HashUtil.hash(newSalt, newPassArray);
                System.out.print("Confirm new password: ");
                if (newPass.equals(HashUtil.hash(newSalt, c.readPassword()))) break;
                System.out.println("Passwords don't match. Please try again");
            }
            String newKey =  HashUtil.generateEncryptionKey(u.getUsername().toCharArray(), newPassArray, newSalt);
            try {
                reEncryptPasswords(u.getKey(), newKey);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(0);
            } finally {
                SqliteDB.closeConnection();
            }
            u.setKey(newKey);
            String newSerial = "";
            if (!oldSerialHash.equals("")) {
                newSerial = HashUtil.hash(newSalt, serial.toCharArray());
            }
            SqliteDB.updateMasterPasswordSerial(newPass, newSerial, Base64.encodeBase64String(newSalt));
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Wrong password. Returning to main screen...");
        }

        try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        
    }
    
    private static void reEncryptPasswords(String oldKey, String newKey) throws SQLException {
        RowSetFactory f = RowSetProvider.newFactory();
        CachedRowSet r = f.createCachedRowSet(); // Found this here https://stackoverflow.com/questions/25493837/java-cant-use-resultset-after-connection-close
        r.populate(SqliteDB.allAccountsForUser());
        SqliteDB.closeConnection();
        while (r.next()) {
            String s = r.getString("service");
            String u = r.getString("username");
            String pass = SqliteDB.getAccountPassword(s, u);
            String unencryptedPass = AESUtil.decrypt(pass, oldKey);
            String encryptedPass = AESUtil.encrypt(unencryptedPass, newKey);
            SqliteDB.updatePassword(s, u, encryptedPass);
        }
    }
    
}
