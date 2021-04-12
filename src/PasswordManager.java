public class PasswordManager {
    private User currUser;
    private StartScreen ss;
    private MainScreen ms;

    public PasswordManager() {
        this.ss = new StartScreen();
        this.ms = new MainScreen();
        this.currUser = null;
    }

    // Main function 
    public static void main(String[] args) throws InterruptedException {
        PasswordManager pm = new PasswordManager();
        while (true) {
            pm.startup();
            pm.beginMain();
            pm.logout();
        }

    }

    public User getCurUser() {
        return currUser;
    }

    // Start screen 
    public void startup() throws InterruptedException {
        currUser = ss.startup();
    }

    // Main screen 
    public void beginMain() {
        SqliteDB.setCurrUserID(currUser.getuid());
        StringGenerator.loadDefault();
        ms.begin(currUser);
    }

    // Logs the current user out 
    public void logout() {
        SqliteDB.setCurrUserID(null);
        currUser = null;
    }

}
