import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteDB {

    private static Connection c = null;
    private static boolean hasData = false; 
    private static String uid = null;

    public static void setCurrUserID(String currUserID) {
        uid = currUserID;
    }

    public static ResultSet getSgenConfig() {
        try {
            getConnection();
            Statement s = c.createStatement();
            return s.executeQuery("SELECT length, flags FROM login where rowid="+uid);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void setSgenDefault(int length, String flags){
        try {
            getConnection();
            PreparedStatement prep = c.prepareStatement("UPDATE login SET length=?, flags=? WHERE rowid=?");
            prep.setInt(1, length);
            prep.setString(2, flags);
            prep.setInt(3, Integer.parseInt(uid));
            prep.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        
    }

    public static void eraseRecords() {
        try {
            getConnection();
            Statement s = c.createStatement();
            s.execute("DELETE FROM user" + uid + "_data");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public static ResultSet allAccountsForUser() {
        try {
            getConnection();
            String q = "SELECT service, username from user" + uid + "_data ORDER BY service ASC, username ASC; ";
            Statement s = c.createStatement();
            return s.executeQuery(q);
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return null;


    } 

    public static boolean checkUsernameExists(String username) {
        try {
            getConnection();
            PreparedStatement prep = c.prepareStatement("SELECT * from login where username=?");
            prep.setString(1, username);
            ResultSet res = prep.executeQuery();
            return res.next();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return false;
    }

    public static int updatePassword(String service, String username, String password) {
        try {
            getConnection();
            String q = "UPDATE user" + uid + "_data SET password=? WHERE service=? AND username=?";
            PreparedStatement prep = c.prepareStatement(q);
            prep.setString(1, password);
            prep.setString(2, service);
            prep.setString(3, username);
            return prep.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return 0;
    }

    public static void deleteAccount(String service, String username) {
        try {
            getConnection();
            String q = "DELETE from user" + uid + "_data WHERE service=? AND username=?";
            PreparedStatement prep = c.prepareStatement(q);
            prep.setString(1, service);
            prep.setString(2, username);
            prep.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public static ResultSet getAccountsFromService(String service) {
        try {
            getConnection();
            String q = "SELECT * from user" + uid + "_data WHERE service=?";
            PreparedStatement prep = c.prepareStatement(q);
            prep.setString(1, service);
            return prep.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return null;
    }

    public static String getAccountPassword(String service, String username) {
        try {
            getConnection();
            String q = "SELECT password from user" + uid + "_data WHERE service=? AND username=?";
            PreparedStatement prep = c.prepareStatement(q);
            prep.setString(1, service);
            prep.setString(2, username);
            ResultSet res = prep.executeQuery();
            res.next();
            return res.getString("password");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return null;
    }

    public static boolean checkServiceUsernameExists(String service, String username) {
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
        } finally {
            closeConnection();
        }
        return false; 
    }

    public static ResultSet getUserDetails(String username) {
        try {
            getConnection();
            PreparedStatement prep = c.prepareStatement("SELECT salt, password, rowid, serial, f1 from login where username=?");
            prep.setString(1, username);
            ResultSet res = prep.executeQuery();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        } 

        return null;
    }

    public static void addAccount(String service, String username, String password) {
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
        } finally {
            closeConnection();
        }
    }

    public static int getCurrentFingerprint() {
        try {
            getConnection();
            Statement s = c.createStatement();
            ResultSet res = s.executeQuery("SELECT value FROM misc where key='fingerprintCounter'");
            res.next();
            return res.getInt("value");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

        return 0;
    }

    public static void updateCurrentFingerPrint() {
        try {
            getConnection();
            Statement s2 = c.createStatement();
            s2.executeUpdate("UPDATE misc SET value = value + 1 where key='fingerprintCounter'");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public static boolean checkPrints(String username, int fID) {
        try {
            getConnection();
            PreparedStatement prep = c.prepareStatement("SELECT * from login where username=? AND ? in (f1, f2, f3, f4, f5)");
            prep.setString(1, username);
            prep.setInt(2, fID);
            ResultSet res = prep.executeQuery();
            return res.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return false;
        
    }

    public static String addUser(String username, String password, String salt, String serial, int fID) {
        try {   
            getConnection();
            PreparedStatement prep = c.prepareStatement("INSERT into login(username, password, salt, length, flags, serial, f1) values(?, ?, ?, ?, ?, ?, ?)");
            prep.setString(1, username);
            prep.setString(2, password);
            prep.setString(3, salt);
            prep.setInt(4, 16);
            prep.setString(5, "1110");
            prep.setString(6, serial);
            prep.setInt(7, fID);
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
        } finally {
            closeConnection();
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
                    state2.execute("CREATE TABLE login(INTEGER PRIMARY KEY, username TEXT, password TEXT, salt TEXT, length INT, flags TEXT, serial TEXT, f1 INT, f2 INT, f3 INT, f4 INT, f5 INT)");
                    Statement state3 = c.createStatement();
                    state3.execute("CREATE TABLE misc(key TEXT, value INT)");
                    Statement state4 = c.createStatement();
                    state4.execute("INSERT INTO misc(key, value) values('fingerprintCounter', 1)");
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
