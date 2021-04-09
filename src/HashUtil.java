import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Hex;

public class HashUtil {
    // Generates a random salt
    public static byte[] saltGen() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Hashes a password and returns the hex string
    public static String hashPassword(byte[] salt, char[] pass){
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec passSpec = new PBEKeySpec(pass, salt, 65536, 256);
            byte[] hash1 = factory.generateSecret(passSpec).getEncoded();
            return Hex.encodeHexString(hash1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    // Generates the user's encrytpion key of their username + password
    public static String generateEncryptionKey(char[] username, char[] password, byte[] salt) {
        char[] combined = Arrays.copyOf(username, username.length + password.length);
        System.arraycopy(password, 0, combined, username.length, password.length);
        return hashPassword(salt, combined);
    }

}
