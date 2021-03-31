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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class AESUtil {

    private static byte[] generateIV() throws NoSuchAlgorithmException{
        SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        secRand.nextBytes(iv);

        return iv;
    }

    public static String encrypt(String password, String keyString) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] iv = generateIV();
            SecretKeySpec sk = new SecretKeySpec(Hex.decodeHex(keyString), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, sk, ivSpec);
            byte[] encrypted = cipher.doFinal(password.getBytes());
            byte[] combined = Arrays.copyOf(iv, iv.length + encrypted.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.encodeBase64String(combined);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public static String decrypt(String encrypted, String keyString) {
        byte[] encryptedBytes = Base64.decodeBase64(encrypted);
        byte[] iv = Arrays.copyOfRange(encryptedBytes, 0, 16);
        byte[] encryption = Arrays.copyOfRange(encryptedBytes, 16, encryptedBytes.length);
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            SecretKeySpec sk = new SecretKeySpec(Hex.decodeHex(keyString), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, sk, ivSpec);
            byte[] original = cipher.doFinal(encryption);
            
            return new String(original);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
