import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.json.simple.parser.ParseException;

public class PasswordManager {
    private User currUser;
    private StartScreen ss;
    private MainScreen ms;

    public PasswordManager() {
        this.ss = new StartScreen();
        this.ms = new MainScreen();
        this.currUser = null;
    }

    public static void main(String[] args) throws Exception {
        PasswordManager pm = new PasswordManager();
        while (true) {
            pm.startup();
            pm.initializeUser();
            pm.beginMain();
            pm.logout();
        }

    }

    public User getCurUser() {
        return currUser;
    }

    public void startup() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ParseException, InterruptedException {
        currUser = ss.startup();
    }

    public void initializeUser(){
        currUser.loadData();
    }

    public void beginMain() {
        ms.begin(currUser);
    }

    public void logout() {
        currUser = null;
    }

}
