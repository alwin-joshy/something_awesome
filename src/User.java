import java.util.ArrayList;

public class User {
    private String username; 
    private String usernameHash;
    private String uid;
    private String key; // Encryption key

    public User(String username, String usernameHash, String uid) {
        this.username = username; 
        this.usernameHash = usernameHash;
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public String getUsernameHash() {
        return usernameHash;
    }
    
    public String getuid() {
        return uid;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
