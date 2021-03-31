import java.io.Console;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.apache.commons.codec.binary.Base64;

public class StartScreen {
    private User currUser;

    public User startup() throws IOException, InterruptedException {
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

    private boolean createUser() throws IOException, InterruptedException {
        Common.clearTerminal();
        Common.fancyBanner("Create a new account");
        Scanner s = new Scanner(System.in);
        boolean done = false;
        String username = "";

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
        String password = new String("");
        String hash1String = new String("");

        // Gets password, hashes them, and checks that they match
        while (!done) {

            System.out.print("Enter password: ");
            char[] passwordArray = c.readPassword();
            password = passwordArray.toString();
            hash1String = HashUtil.hashPassword(salt, passwordArray);

            System.out.print("Confirm password: ");
            String hash2String = HashUtil.hashPassword(salt, c.readPassword());

            if (!hash1String.equals(hash2String)) {
                System.out.println("Passwords don't match! Try again");
            } else {
                done = true;

            }
        }

        // Linking arduino 
        System.out.println("Would you like to link an Arduino to your account? If you do this, your account can only be unlocked when this device is connected.");
        System.out.print("Press enter for yes or any other button for no: ");
        String response = s.nextLine();

        String serial = "";
        String serialHash = "";
        if (response.equals("")) {
            String serialReturned = ArduinoUtil.addArduino();
            if (serialReturned.equals("")) {
                return false;
            }
            serial = serialReturned;
            serialHash = HashUtil.hashPassword(salt, serial.toCharArray());
        }

        int fID = 0;
        // Adding fingerprint authentication 
        if (!serial.equals("")) {
            System.out.print("Press enter to add fingerprint verification or any other button to not: ");
            response = s.nextLine();
            if (response.equals("")) {
                System.out.println("\nPlease hold a finger on the sensor. You can add more fingers later");
                System.out.println("\nChecking if fingerprint already registered\n");
                fID = ArduinoUtil.getFingerprint(serialHash, salt);
                System.out.println(fID);
                if (fID == 0) {
                    fID = ArduinoUtil.addFingerprint(serialHash, salt);
                    if (fID == 0) {
                        Thread.sleep(2000);
                        return false;
                    }
                } else if (fID == -1) {
                    Thread.sleep(2000);
                    return false;
                }
                
            }
        }        

        // Adds to database
        String uid = SqliteDB.addUser(username, hash1String, saltString, serialHash, fID);
        System.out.println("Account creation successful!");
        currUser = new User(username, uid);
        // Generates and sets encryption key 
        String key = HashUtil.generateEncryptionKey(username.toCharArray(), password.toCharArray(), salt);
        currUser.setKey(key);
        Thread.sleep(1000);
        return true; 
        
    }

   
    // Allows user to log in 
    private boolean login() throws IOException, InterruptedException {
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
                int fingerID = 0;
                try {
                    salt = res.getString("salt");
                    actualPass = res.getString("password");
                    uid = res.getString("rowid");
                    serial = res.getString("serial");
                    fingerID = res.getInt("f1");
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
                    // If a fingerprint has been set 
                    if (fingerID != 0) {
                        fingerID = ArduinoUtil.getFingerprint(serial, saltArray);
                        if (fingerID == - 1 || fingerID == 0) {
                            Thread.sleep(2000);
                            return false;
                        }
                        if (!SqliteDB.checkPrints(username, fingerID)) {
                            System.out.println("Fingerprint does not match any registered fingerprints. Returning to main menu");
                            Thread.sleep(2000);
                            return false;
                        }
                    } else if (!serial.equals("")) { // If unique arduino authentication has been set
                        if (ArduinoUtil.checkArduinoConnection(serial, saltArray) == null) {
                            return false;
                        }
                    }

                    System.out.println("\nLogin successful!");
                    // Generates and sets encryption key 
                    String key = HashUtil.generateEncryptionKey(username.toCharArray(), password, saltArray); 
                    currUser.setKey(key);
                    Thread.sleep(1000);

                    return true; 
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

        return false; 
    }
}
