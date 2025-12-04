package com.radyfy.common.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Component
public class PasswordHash {

    @Value("${app.authentication.salt}")
    private String salt;

    public String hashPassword(String password) {
        String pw = "";
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt.getBytes(),
                10000,
                32 * 8
        );
        SecretKeyFactory key = null;
        try {
            key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            pw = Base64.getEncoder().encodeToString(key.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return pw;
    }

    public static void mains(String[] args) {
        PasswordHash ph = new PasswordHash();
        ph.salt = "shdskUIYY*(F)#__3-if29fu3-";
        System.out.println(ph.hashPassword("Pintu@123"));
    }

}
