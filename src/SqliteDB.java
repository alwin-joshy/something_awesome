import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteDB {

    private static Connection c = null;
    private static boolean hasData = false; 

    public SqliteDB() {
        getConnection();
    }
    
    public boolean checkUsernameExists(String username) {
        try {
            PreparedStatement prep = c.prepareStatement("SELECT * from login where username=?");
            prep.setString(1, username);
            ResultSet res = prep.executeQuery();
            return res.next();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet getSaltAndHash(String username) {
        try {
            PreparedStatement prep = c.prepareStatement("SELECT salt, password from login where username=?");
            prep.setString(1, username);
            ResultSet res = prep.executeQuery();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addUser(String username, String password, String salt) {
        try {   
            PreparedStatement prep = c.prepareStatement("INSERT into login values(?, ?, ?)");
            prep.setString(1, username);
            prep.setString(2, password);
            prep.setString(3, salt);
            prep.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("JDBC:sqlite:PasswordManager.db");
            initialise();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private void initialise() {
        if (!hasData) {
            hasData = true;
            
            try{
                Statement state = c.createStatement();
                ResultSet res = state.executeQuery("SELECT name from sqlite_master WHERE type='table' and name='login'");
                if (!res.next()) {
                    Statement state2 = c.createStatement();
                    state2.execute("CREATE TABLE login(username TEXT, password TEXT, salt TEXT)");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
