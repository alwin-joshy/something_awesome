import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class StringGenerator {
    private int length;
    private boolean lc;
    private boolean uc;
    private boolean num;
    private boolean symb;
    private ArrayList<Character> defaultCharList;

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

    public void setDefaultInteractive() {
        int length = getGeneratorLength();
        ArrayList<Boolean> v = getGeneratorConfig();
        setDefault(length, v.get(0), v.get(1), v.get(2), v.get(3));
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

    private int getGeneratorLength() {
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

    private ArrayList<Boolean> getGeneratorConfig() {
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
