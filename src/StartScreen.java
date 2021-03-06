import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Scanner;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StartScreen {
    private User currUser;

    public User startup() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ParseException, InterruptedException {
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

    private boolean createUser() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ParseException, InterruptedException {
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
            if (new File("accounts.json").isFile()) {
                JSONParser p = new JSONParser();
                FileReader r = new FileReader("accounts.json");
                accountList = (JSONObject) p.parse(r);
                if (accountList.containsKey(username)) {
                    System.out.println("This username has already been taken. Please try another one.");
                } else {
                    done = true;
                }
            } else {
                done = true;
            }
        }

        // https://stackoverflow.com/questions/8138411/masking-password-input-from-the-console-java
        // Generating the salt
        
        byte[] salt = saltGen();
        String saltString = Base64.encodeBase64String(salt);
        done = false; 
        Console c =  System.console();
        String hash1String = new String("");

        // Gets password, hashes them, and checks that they match
        while (!done) {

            System.out.print("Enter password: ");
            hash1String = hashPassword(salt, c.readPassword());

            System.out.print("Confirm password: ");
            String hash2String = hashPassword(salt, c.readPassword());

            if (!hash1String.equals(hash2String)) {
                System.out.println("Passwords don't match! Try again");
            } else {
                done = true;

            }
        }

        // Writes updated accounts list to JSON file
        JSONObject data = new JSONObject();
        data.put("salt", saltString);
        data.put("password", hash1String);
        JSONObject accounts;
        accountList.put(username, data);
        try {
            FileWriter f = new FileWriter("accounts.json");
            f.write(accountList.toJSONString());
            f.flush();
            f.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

        System.out.println("Account creation successful!");
        currUser = new User(username);
        Thread.sleep(1000);

        return true; 
        
    }

    private boolean login() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ParseException {
        Common.clearTerminal();
        Scanner s = new Scanner(System.in);
        Console c =  System.console();
        boolean invalid = true; 
        Common.fancyBanner("Login");
        while (invalid) {
            System.out.print("Enter username: ");
            String username = s.nextLine();
            System.out.print("Enter password: ");
            // If there are no users yet, there is obviously no way they can sign in
            if (new File("accounts.json").isFile()) {
                JSONParser p = new JSONParser();
                FileReader r = new FileReader("accounts.json");
                JSONObject accounts = (JSONObject) p.parse(r);
                // If the username isn't a key in the JSON file, the user cannot exist
                if (accounts.containsKey(username)){
                    JSONObject user = (JSONObject) accounts.get(username);
                    String salt = (String) user.get("salt");
                    String actualPassword = (String) user.get("password");
                    byte[] saltArray = Base64.decodeBase64(salt);

                    String enteredPassword = hashPassword(saltArray, c.readPassword());
                    // Compares the hash of the entered password with that of the actual password
                    if (enteredPassword.equals(actualPassword)) {
                        currUser = new User(username);
                        invalid = false;
                    }
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

    // Generates a random salt
    private byte[] saltGen() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Hashes a password and returns the hex string
    private String hashPassword(byte[] salt, char[] pass) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec passSpec = new PBEKeySpec(pass, salt, 65536, 128);
        byte[] hash1 = factory.generateSecret(passSpec).getEncoded();
        return Hex.encodeHexString(hash1);
    }
}
