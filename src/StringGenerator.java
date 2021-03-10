import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class StringGenerator {
    private int length;
    private boolean lc;
    private boolean uc;
    private boolean num;
    private boolean symb;
    private ArrayList<Character> defaultCharList;

    public StringGenerator(){
        setDefault(14, true, true, true, false);
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

}
