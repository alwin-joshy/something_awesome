import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Common {

        // Erases the terminal
    public static void clearTerminal(){
        System.out.print("\033[H\033[2J"); //Found here https://www.reddit.com/r/java/comments/1uuvqo/clear_terminal_screen_linux/
    }

    // Makes a nice looking banner
    public static void fancyBanner(String msg) {
        for (int i = 0; i < msg.length() + 4; i++) {
            System.out.print("~");
        }
        System.out.println();
        System.out.println("~ " + msg + " ~");
        for (int i = 0; i < msg.length() + 4; i++) {
            System.out.print("~");
        }
        System.out.println("\n");

    }

    // Checks a password based on the NIST standard of min. 8 characters long and uncommon
    public static boolean checkPassword(String password) {
        if (password.length() < 8) {
            System.out.println("Password is too short. The reccommended length is at least 8 characters"); 
            return false;
        }

        File f = new File("passwords.txt");

        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                line.strip();
                if (line.equals(password)) {
                    System.out.println("Password is too common and can be easily guessed");
                    sc.close();
                    return false;
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false; 
        } 

        return true;
    }
    
}
