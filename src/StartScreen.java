import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.print.attribute.standard.Fidelity;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StartScreen {
    private User currUser;

    public User startup() throws IOException, ParseException, InterruptedException {
        // Show initial options         
        Scanner s = new Scanner(System.in);

        Boolean done = false; 
        while (!done) {
            Common.clearTerminal();
            Common.fancyBanner("Welcome to Awesome Password Manager");
            System.out.println("1 - Sign in ");
            System.out.println("2 - Create an account");
            System.out.println("q - Quit");
            System.out.print("Enter command: ");
            String command = s.nextLine();
            if (command.equals("1")) {
                if (login()) {
                    done = true;
                }
            } else if (command.equals("2")) {
                if (createUser()) {
                    done = true;
                }
            } else if (command.equals("q")) {
                System.exit(0);
            } else {
                System.out.println("Invalid command - please try again");
            }
        }
        
        return currUser;
    } 

    private boolean createUser() throws IOException, ParseException, InterruptedException {
        Common.clearTerminal();
        Common.fancyBanner("Create a new account");
        Scanner s = new Scanner(System.in);
        boolean done = false;
        String username = "";
        JSONObject accountList = new JSONObject();

        // Checks that user does not already exist 
        while (!done){
            System.out.print("Enter username: ");
            username = s.nextLine();
            if (SqliteDB.checkUsernameExists(username)) {
                System.out.println("This username has already been taken. Please try another one.");
            } else {
                done = true;
            }
        }

        // https://stackoverflow.com/questions/8138411/masking-password-input-from-the-console-java
        // Generating the salt
        
        byte[] salt = HashUtil.saltGen();
        String saltString = Base64.encodeBase64String(salt);
        done = false; 
        Console c =  System.console();
        String hash1String = new String("");

        // Gets password, hashes them, and checks that they match
        while (!done) {

            System.out.print("Enter password: ");
            hash1String = HashUtil.hashPassword(salt, c.readPassword());

            System.out.print("Confirm password: ");
            String hash2String = HashUtil.hashPassword(salt, c.readPassword());

            if (!hash1String.equals(hash2String)) {
                System.out.println("Passwords don't match! Try again");
            } else {
                done = true;

            }
        }

        System.out.println("Would you like to link an Arduino to your account? If you do this, your account can only be unlocked when this device is connected.");
        System.out.print("Press enter for yes or any other button for no: ");
        String response = s.nextLine();

        String serial = "";
        if (response.equals("")) {
            String serialReturned = ArduinoUtil.addArduino();
            if (serialReturned.equals("")) {
                return false;
            }
            serial = serialReturned;
        }

        String serialHash = HashUtil.hashPassword(salt, serial.toCharArray());

        int fID = 0;
        if (!serial.equals("")) {
            System.out.print("Press enter to add fingerprint verification or any other button to not: ");
            response = s.nextLine();
            if (response.equals("")) {
                fID = ArduinoUtil.addFingerprint(serialHash, salt);
                if (fID == 0) {
                    Thread.sleep(2000);
                    return false;
                }
            }
        }        

        // Adds to database
        String uid = SqliteDB.addUser(username, hash1String, saltString, serialHash, fID);

        System.out.println("Account creation successful!");
        currUser = new User(username, uid);
        Thread.sleep(1000);

        return true; 
        
    }

    private boolean login() throws IOException, ParseException, InterruptedException {
        Common.clearTerminal();
        Scanner s = new Scanner(System.in);
        Console c =  System.console();
        boolean invalid = true; 
        Common.fancyBanner("Login");
        while (invalid) {
            System.out.print("Enter username: ");
            String username = s.nextLine();
            System.out.print("Enter password: ");
            char[] password = c.readPassword();
            // If there are no users yet, there is obviously no way they can sign in
            if (SqliteDB.checkUsernameExists(username)) {
                ResultSet res = SqliteDB.getUserDetails(username);
                String salt = "";
                String actualPass = "";
                String uid = "";
                String serial = "";
                try {
                    salt = res.getString("salt");
                    actualPass = res.getString("password");
                    uid = res.getString("rowid");
                    serial = res.getString("serial");
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    SqliteDB.closeConnection();
                }
                byte[] saltArray = Base64.decodeBase64(salt);
                String enteredPassword = HashUtil.hashPassword(saltArray, password);
                // Compares the hash of the entered password with that of the actual password
                if (enteredPassword.equals(actualPass)) {
                    currUser = new User(username, uid);
                    invalid = false;
                    if (!serial.equals("")) {
                        if (ArduinoUtil.checkArduinoConnection(serial, saltArray) == null) {
                            return false;
                        }
                    }
                    System.out.println("\nLogin successful!");
                    Thread.sleep(1000);
                }

            }
            if (invalid) {
                System.out.println("Invalid username/password combination. Press 1 to try again or any other key to return to the previous screen");
                String command = s.nextLine();
                if (!command.equals("1")) {
                    break;
                }
            }
        }

        return !invalid; 
    }
}
