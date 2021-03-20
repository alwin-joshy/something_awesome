import java.util.ArrayList;

public class User {
    //private ArrayList<Services> services;
    private String username; 
    private String uid;

    public User(String username, String uid) {
        this.username = username; 
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }
    
    public String getuid() {
        return uid;
    }

}
