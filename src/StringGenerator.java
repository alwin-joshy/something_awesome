import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class StringGenerator {
    private static int length;
    private static boolean lc;
    private static boolean uc;
    private static boolean num;
    private static boolean symb;
    private static ArrayList<Character> defaultCharList;

    // Default generator
    public static String generateDefault(){
        return generate(length, defaultCharList);
    }

    // Custom generator 
    public static String generateCustom(int length, boolean lc, boolean uc, boolean num, boolean symb) {
        return generate(length, generateCharList(lc, uc, num, symb));
    }   

    // Loads default values into the generator 
    public static void loadDefault() {
        ResultSet res = SqliteDB.getSgenConfig();
        try {
            res.next();
            int length = res.getInt("length");
            ArrayList<Boolean> v = processFlags(res.getString("flags"));
            setDefault(length, v.get(0), v.get(1), v.get(2), v.get(3));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqliteDB.closeConnection();
        }

    }

    // Generates random string 
    public static String generate(int length, ArrayList<Character> chars) {
        Random r = new Random();
        int i = 0;
        String pass = new String();
        while (i < length){
            pass = pass + chars.get(r.nextInt(chars.size()));
            i++;
        }
        return pass;
    }

    public static int getLength(){
        return length;
    }

    public static boolean getlc() {
        return lc;
    }

    public static boolean getuc() {
        return uc;
    }

    public static boolean getNum() {
        return num;
    }

    public static boolean getSymb(){
        return symb;
    }


    private static void setDefault(int lengthIn, boolean lcIn, boolean ucIn, boolean numIn, boolean symbIn) {
        length = lengthIn;
        lc = lcIn;
        uc = ucIn;
        num = numIn;
        symb = symbIn;
        defaultCharList = generateCharList(lc, uc, num, symb);
    }
    
    // Generates character list which meets the flasg provided 
    public static ArrayList<Character> generateCharList(boolean lc, boolean uc, boolean num, boolean symb) {
        ArrayList<Character> charList = new ArrayList<Character>();
        if (lc) {
            for (char i = 'a'; i <= 'z'; i++) {
                charList.add(i);
            }
        }
        if (uc) {
            for (char i = 'A'; i <= 'Z'; i++) {
                charList.add(i);
            }
        }
        if (num) {
            for (char i = '0'; i <= '9'; i++) {
                charList.add(i);
            }
        }
        if (symb) {
            charList.addAll(Arrays.asList('~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=', '[', ']', '{', '}', ';', ':',
                                          '<', '>', '.', ',', '/', '?', '\\', '|'));
        }

        return charList;
    }

    // Interactive menu to generate a string 
    public static String generateString() {
        System.out.println("Would you like to use the default configuration (" + getLength() + " characters, lc letters=" + getlc() +
                            ", uc letters=" + getuc() + ", numbers=" + getNum() + ", symbols=" + getSymb() + ")?");
        System.out.print("Press enter for the default and any other key for custom: ");
        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        if (input.equals("")) {
            return generateDefault();
        } else {
            int length = getGeneratorLength();
            ArrayList<Boolean> v = getGeneratorConfig();
            return generateCustom(length, v.get(0), v.get(1), v.get(2), v.get(3));
        }
    }

    // Interactive menu to set the default
    public static void setDefaultInteractive() {
        int length = getGeneratorLength();
        ArrayList<Boolean> v = getGeneratorConfig();
        SqliteDB.setSgenDefault(length, createBinaryString(v));
        setDefault(length, v.get(0), v.get(1), v.get(2), v.get(3));
    }

    // Creates binary string from flags 
    private static String createBinaryString(ArrayList<Boolean> v){
        String res = "";
        for (int i = 0; i < 4; i++) {
            res = res + (v.get(i) == true ? 1 : 0);
        }
        return res;
    }

    // Processes binary string to create boolean array for options 
    private static ArrayList<Boolean> processFlags(String flags) {
        if (flags.equals("0000")) return null;
        if (flags.length() != 4) return null;
        ArrayList<Boolean> values = new ArrayList<Boolean>();
        for (int i = 0; i < 4; i++) {
            if (flags.charAt(i) != '1' && flags.charAt(i) != '0') return null;
            values.add(flags.charAt(i) == '1' ? true : false);
        }
        return values;
   }

    private static int getGeneratorLength() {
        int length;
        Scanner s = new Scanner(System.in);
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

    private static ArrayList<Boolean> getGeneratorConfig() {
        ArrayList<Boolean> v;
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.print("Enter binary string for lowercase, uppercase, numbers, and symbols e.g. 1000 for only lc letters: ");
            String flags = s.nextLine();
            v = processFlags(flags);
            if (v != null) {
                break;
            }
            System.out.println("Please entre a valid binary string");
        }
        return v;
    }



}
