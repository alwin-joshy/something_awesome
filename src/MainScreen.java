import java.util.ArrayList;
import java.util.Scanner;

public class MainScreen {

    private StringGenerator sGen;

    public MainScreen() {
        sGen = new StringGenerator();
    }

    public void begin(User u) {
        Common.clearTerminal();
        Common.fancyBanner("Hi " + u.getUsername() + "! Press ? to view commands");
        System.out.print("Enter command: ");
        Scanner s = new Scanner(System.in);
        String command = s.nextLine();
        while (!command.equals("q")) {
            switch (command) {
                case "?":
                    printCommands();
                case "a": 
                    //addAccount();
                case "c":
                    //changeAccount();
                case "v":
                    //viewAccounts();
                case "p":
                    //fetchCredentials();
                case "g":
                    generateString();
                case "s":
                    settings();
            }

            System.out.print("Enter command: ");
            command = s.nextLine();
        }

        System.out.println("Enter x to quit and any other button to return to the login screen");
        System.out.print("Enter command: ");
        command = s.nextLine();

        if (command.equals("x")) {
            System.exit(1);
        }
    }
    
    public void settings(){
        System.out.println("Enter g to set the default configuration for the random password generator");
        Scanner s = new Scanner(System.in);
        String command = s.nextLine();
        switch (command) {
            case "g":
                int length = getGeneratorLength();
                Arraylist<Boolean> v = getGeneratorConfig();
                sGen.setDefault(length, v.get(0), v.get(1), v.get(2), v.get(3));
        }
    }

    public void generateString() {
        System.out.println("Would you like to use the default configuration (" + sGen.getLength() + " characters, lc letters=" + sGen.getlc() +
                            ", uc letters=" + sGen.getuc() + ", numbers=" + sGen.getNum() + ", symbols=" + sGen.getSymb() + ")? y/n");
        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        if (input.equals("y")) {
            System.out.println(sGen.generateDefault());
        } else if (input.equals("n")) {
            int length = getGeneratorLength();
            Arraylist<Boolean> v = getGeneratorConfig();
            System.out.println(sGen.generateCustom(length, v.get(0), v.get(1), v.get(2), v.get(3)));
        }
    }
   
    
    private int getGeneratorLength() {
        int length;
        while (true) {
            System.out.print("Enter desired length (5-128): ");
            length = s.nextInt();
            if (length >= 5 && length <= 128) {
                break;
            }
            System.out.println("Desired length must be between 5 and 128 characters long");
        }
        return length;
   }
    
   private String getGeneratorConfig() {
        while (true) {
            System.out.print("Enter binary string for lowercase, uppercase, numbers, and symbols e.g. 1000 for only lc letters");
            String flags = s.nextLine();
            ArrayList<Boolean> v = processFlags(flags);
            if (v != null) {
                break;
            }
            System.out.println("Please entre a valid binary string");
        }
        return v;
       
   }

   private ArrayList<Boolean> processFlags(String flags) {
        if (flags.equals("0000")) return null;
        if (flags.length() != 4) return null;
        ArrayList<Boolean> values = new ArrayList<Boolean>();
        for (int i = 0; i < 4; i++) {
            if (flags.charAt(i) != '1' && flags.charAt(i) != '0') return null;
            values.add(flags.charAt(i) == '1' ? true : false);
        }
        return values;
   }

    public void printCommands() {
        System.out.println("a - add new account\n"+
                           "c - change account details\n"+
                           "v - view all accounts\n"+
                           "p - search for account credentials\n"+
                           "g - generate random string\n"+
                           "s - settings\n"+
                           "q - logout\n");
    }
}
