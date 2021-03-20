import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base16;
import org.apache.commons.codec.binary.Base64;

public class AESUtil {
    private static final String keyString = "s5v8y/B?E(H+MbQeShVmYq3t6w9z$C&F";

    private static byte[] generateIV(Cipher cipher) throws NoSuchAlgorithmException{
        SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        secRand.nextBytes(iv);

        return iv;
    }

    public static String encrypt(String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] iv = generateIV(cipher);
            SecretKeySpec sk = new SecretKeySpec(keyString.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, sk, ivSpec);
            byte[] encrypted = cipher.doFinal(password.getBytes());
            return Base64.encodeBase64String(iv) + Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decrypt(String encrypted) {
        byte[] encryptedBytes = Base64.decodeBase64(encrypted);
        byte[] iv = Arrays.copyOfRange(encryptedBytes, 0, 15);
        byte[] encryption = Arrays.copyOfRange(encryptedBytes, 16, encryptedBytes.length - 1);
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            SecretKeySpec sk = new SecretKeySpec(keyString.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, sk, ivSpec);
            byte[] original = cipher.doFinal(encryption);
    
            return new String(original);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
