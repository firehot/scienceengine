package com.mazalearn.scienceengine.app.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.badlogic.gdx.Gdx;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

public class Crypter {
  private static final String SALT = "imazalearne";
  private static final String XLATE = "0123456789abcdef";
  
  public interface Sha1 {
    public byte[] sha1Hash(byte[] toHash);
  }
  
  // default sha1 implementor
  private static Sha1 sha1 = new Sha1() {
      public byte[] sha1Hash(byte[] toHash) {
        try {
          MessageDigest digest = MessageDigest.getInstance("SHA-1");
          digest.update(toHash, 0, toHash.length);
          return digest.digest();
        } catch (NoSuchAlgorithmException e) {
          if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
            e.printStackTrace();
          }
          Gdx.app.error(ScienceEngine.LOG, "Could not compute hash: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
          if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
            e.printStackTrace();
          }
          Gdx.app.error(ScienceEngine.LOG, "Could not compute hash: " + e.getMessage());      
        }
        return null;
      }
    };
  
  public static void setSha1Implementor(Sha1 sha1Implementor) {
    sha1 = sha1Implementor;
  }
  
  // Produces a 160 bit message digest = 20 bytes.
  // Each byte is converted to 2 hexchars => 40 char string.
  // package protected for testing
  public static String saltedSha1Hash(String target, String id) {
    String toHash = id + SALT + target + id + SALT;
    byte[] bytes;
    try {
      bytes = toHash.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
        e.printStackTrace();
      }
      Gdx.app.error(ScienceEngine.LOG, "Could not compute hash: " + e.getMessage());
      return null;
    }
    return hexlate(sha1.sha1Hash(bytes));
  }

  private static String hexlate (byte[] bytes) {
    if (bytes == null) return "";

    int count = bytes.length;
    char[] chars = new char[count*2];

    for (int i = 0; i < count; i++) {
      int val = bytes[i];
      if (val < 0) {
          val += 256;
      }
      chars[2*i] = XLATE.charAt(val/16);
      chars[2*i+1] = XLATE.charAt(val%16);
    }

    return new String(chars);
  }
}
