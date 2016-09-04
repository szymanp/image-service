package com.metapx.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface PasswordManager {

  public String computeHash(String password, String salt);
  
  public class Default implements PasswordManager {

    private static final String ALGO = "SHA-512";
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    
    @Override
    public String computeHash(String password, String salt) {
      try {
        MessageDigest md = MessageDigest.getInstance(ALGO);
        String concat = (salt == null ? "" : salt) + password;
        byte[] bHash = md.digest(concat.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(bHash);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }

    public static String bytesToHex(byte[] bytes) {
      char[] chars = new char[bytes.length * 2];
      for (int i = 0; i < bytes.length; i++) {
        int x = 0xFF & bytes[i];
        chars[i * 2] = HEX_CHARS[x >>> 4];
        chars[1 + i * 2] = HEX_CHARS[0x0F & x];
      }
      return new String(chars);
    }
  }
}
