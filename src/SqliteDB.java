import java.io.IOException;
import java.lang.Thread.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

public class SqliteDB {

    private static Connection c = null;
    private static boolean hasData = false; 

    public static boolean checkUsernameExists(String username) {
        try {
            getConnection();
            PreparedStatement prep = c.prepareStatement("SELECT * from login where username=?");
            prep.setString(1, username);
            ResultSet res = prep.executeQuery();
            return res.next();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkServiceUsernameExists(String uid, String service, String username) {
        try {
            getConnection();
            String q = "SELECT * from user" + uid + "_data WHERE service=? and username=?";
            PreparedStatement prep = c.prepareStatement(q);
            prep.setString(1, service);
            prep.setString(2, username);
            ResultSet res = prep.executeQuery();
            return res.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; 
    }

    public static ResultSet getSaltAndHash(String username) {
        try {
            getConnection();
            PreparedStatement prep = c.prepareStatement("SELECT salt, password, rowid from login where username=?");
            prep.setString(1, username);
            ResultSet res = prep.executeQuery();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addAccount(String uid, String service, String username, String password) {
        try {
            getConnection();
            String query = "INSERT INTO user" + uid + "_data values (?, ?, ?)";
            PreparedStatement prep = c.prepareStatement(query);
            prep.setString(1, service);
            prep.setString(2, username);
            prep.setString(3, password);
            prep.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String addUser(String username, String password, String salt) {
        try {   
            getConnection();
            PreparedStatement prep = c.prepareStatement("INSERT into login(username, password, salt) values(?, ?, ?)");
            prep.setString(1, username);
            prep.setString(2, password);
            prep.setString(3, salt);
            prep.execute();

            prep = c.prepareStatement("SELECT rowid from login where username=?");
            prep.setString(1, username);
            ResultSet res = prep.executeQuery();
            res.next();
            String uid = res.getString("rowid");
            String createTable = "CREATE TABLE user" + uid + "_data(service TEXT, username TEXT, password TEXT)";
            Statement s = c.createStatement();
            s.execute(createTable);
            return uid;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void getConnection() {
        try {
            if (c != null) return;
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("JDBC:sqlite:PasswordManager.db");
            initialise();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private static void initialise() {
        if (!hasData) {
            hasData = true;
            try{
                Statement state = c.createStatement();
                ResultSet res = state.executeQuery("SELECT name from sqlite_master WHERE type='table' and name='login'");
                if (!res.next()) {
                    Statement state2 = c.createStatement();
                    state2.execute("CREATE TABLE login(INTEGER PRIMARY KEY, username TEXT, password TEXT, salt TEXT)");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeConnection() {
        if (c != null) {
            try {
                c.close();
                c = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
