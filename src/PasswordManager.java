import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.json.simple.parser.ParseException;

public class PasswordManager {
    private User currUser;
    private StartScreen sc;

    public PasswordManager() {
        this.sc = new StartScreen();
    }

    public static void main(String[] args) throws Exception {
        PasswordManager pm = new PasswordManager();
        pm.startup();
        pm.initializeUser();
    }

    public void startup() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ParseException, InterruptedException {
        currUser = sc.startup();
    }

    public void initializeUser(){
        currUser.loadData();
    }

}
