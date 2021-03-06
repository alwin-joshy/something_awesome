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

    // Sets the userID for queries
    public static void setCurrUserID(String currUserID) {
        uid = currUserID;
    }

    // Gets configuration of random string generator for user 
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

    // Sets the default values for the string generator in the DB 
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

    // Erases all the accounts stored for a user
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

    // Returns an ordered list of all the accounts that the user has 
    public static ResultSet allAccountsForUser() {
        try {
            getConnection();
            String q = "SELECT service, username, password from user" + uid + "_data ORDER BY service ASC;";
            Statement s = c.createStatement();
            return s.executeQuery(q);
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return null;

    } 

    // Checks if the username already exists in the PM 
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

    // Updates the password of a stored account  
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

    // Updating username based on password
    public static int updateUsername(String service, String pass, String newUser) {
        try {
            getConnection();
            String q = "UPDATE user" + uid + "_data SET username=? WHERE service=? AND password=?";
            PreparedStatement prep = c.prepareStatement(q);
            prep.setString(1, newUser);
            prep.setString(2, service);
            prep.setString(3, pass);
            return prep.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        } 

        return 0;
    }

    // Deletes a specified account 
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

    // Gets all the accounts registered under a service
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

    // Gets the encrypted password for a specified account 
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

    // Checks if a username already exists within a service
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

    // Gets all the required details of the user for login 
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

    // Adds a new account to a service 
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

    // Updates the master password and salt for the password manager 
    public static void updateMasterPasswordSerial(String newPass, String newSerial, String newSalt) {
        try {
            getConnection();
            String q = "UPDATE login SET password=?, salt=?, serial=? WHERE rowid=?";
            PreparedStatement prep = c.prepareStatement(q);
            prep.setString(1, newPass);
            prep.setString(2, newSalt);
            prep.setString(3, newSerial);
            prep.setInt(4, Integer.parseInt(uid));
            prep.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // Gets the counter for which fingerprint the database is up to 
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

    // Gets which slot to store the fingerprint in 
    public static int getFingerprintSlot() {
        try {
            int i = 1;
            Statement s = c.createStatement();
            ResultSet r = s.executeQuery("SELECT f1, f2, f3, f4, f5 from login where rowid=" + uid);
            while (i <= 5) {
                if (r.getInt("f"+i) == 0) {
                    return i;
                }
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

        return 0;
    }

    // Updates a fingerprint slot to store the correct fingerprint ID 
    public static void updateFingerprintSlot(int slot, int fID) {
        try {
            getConnection();
            PreparedStatement p = c.prepareStatement("UPDATE login SET f"+ slot + " = ? where rowid="+uid);
            p.setInt(1, fID);
            p.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // Updates the serial number of the device used to unlock the account 
    public static void updateSerial(String serial) {
        try {
            getConnection();
            PreparedStatement p = c.prepareStatement("UPDATE login SET serial=? where rowid="+uid);
            p.setString(1, serial);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // Increments the fingerprint counter
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

    // Checks if fignerprint is a match for a user
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

    // Adds a new user to the PM 
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

    // Establishes connection with the SQL DB 
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

    // Intializes the connection with the database
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

    // Closes connection to the SQL server
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
