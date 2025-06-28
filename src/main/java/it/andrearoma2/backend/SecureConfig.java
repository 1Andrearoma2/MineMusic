package it.andrearoma2.backend;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Properties;

public class SecureConfig {

    private static final String key = "8235987214539870";

    public static String clientId;
    public static String clientSecret;
    public static String redirectUri;

    static {
        InputStream is = SecureConfig.class.getClassLoader().getResourceAsStream("config.enc");
        if (is == null) System.err.println("File config.enc non trovato!");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
            Properties decryptedProps = new Properties();
            String line;
            while((line = reader.readLine()) != null){
                String[] kv = line.split("=");
                if (kv.length == 2){
                    decryptedProps.setProperty(kv[0], decrypt(kv[1]));
                }
            }
            clientId = decryptedProps.getProperty("clientId");
            clientSecret = decryptedProps.getProperty("clientSecret");
            redirectUri = decryptedProps.getProperty("redirectUri");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String decrypt(String strToDecrypt) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }
}
