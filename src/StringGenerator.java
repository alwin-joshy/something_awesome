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

    public StringGenerator(){
        setDefault(16, true, true, true, false);
    }

    public String generateDefault(){
        return generate(length, defaultCharList);
    }

    public String generateCustom(int length, boolean lc, boolean uc, boolean num, boolean symb) {
        return generate(length, generateCharList(lc, uc, num, symb));
    }   

    public String generate(int length, ArrayList<Character> chars) {
        Random r = new Random();
        int i = 0;
        String pass = new String();
        while (i < length){
            pass = pass + chars.get(r.nextInt(chars.size()));
            i++;
        }
        return pass;
    }

    public int getLength(){
        return length;
    }

    public boolean getlc() {
        return lc;
    }

    public boolean getuc() {
        return uc;
    }

    public boolean getNum() {
        return num;
    }

    public boolean getSymb(){
        return symb;
    }


    public void setDefault(int length, boolean lc, boolean uc, boolean num, boolean symb) {
        this.length = length;
        this.lc = lc;
        this.uc = uc;
        this.num = num;
        this.symb = symb;
        defaultCharList = generateCharList(lc, uc, num, symb);
    }
    
    public ArrayList<Character> generateCharList(boolean lc, boolean uc, boolean num, boolean symb) {
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

    public String generateString() {
        System.out.println("Would you like to use the default configuration (" + getLength() + " characters, lc letters=" + getlc() +
                            ", uc letters=" + getuc() + ", numbers=" + getNum() + ", symbols=" + getSymb() + ")? y/n");
        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        if (input.equals("y")) {
            return generateDefault();
        } else if (input.equals("n")) {
            int length;
            boolean lc, uc, num, symb;
            while (true) {
                System.out.print("Enter desired length (5-128): ");
                length = s.nextInt();
                if (length >= 5 && length <= 128) {
                    break;
                } 
                System.out.println("Desired length must be between 5 and 128 characters long");
            } while (true) {
                System.out.print("Enter binary string for lowercase, uppercase, numbers, and symbols e.g. 1000 for only lc letters");
                String flags = s.nextLine();
                ArrayList<Boolean> v = processFlags(flags);
                if (v != null) {
                    lc = v.get(0); uc = v.get(1); num = v.get(2); symb = v.get(3);
                    break;
                }
                System.out.println("Please entre a valid binary string");
            }
            return generateCustom(length, lc, uc, num, symb);
        }
        return null;
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

}
