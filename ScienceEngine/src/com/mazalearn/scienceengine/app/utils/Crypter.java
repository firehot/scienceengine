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
  // Produces a 160 bit message digest = 20 bytes.
  // Each byte is converted to 2 hexchars => 40 char string.
  private static String sha1Hash(String toHash) {
    String hash = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef";
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] bytes = toHash.getBytes("UTF-8");
      digest.update(bytes, 0, bytes.length);
      bytes = digest.digest();
      hash = hexlate(bytes);
    } catch (NoSuchAlgorithmException e) {
      if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
        e.printStackTrace();
      }
      Gdx.app.error(ScienceEngine.LOG, "Could not compute hash: " + e.getMessage());
    } catch (UnsupportedEncodingException e) {
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
    return hash;
  }
  
  public static String saltedSha1Hash(String toHash, String id) {
    return sha1Hash(id + SALT + toHash + id + SALT);
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
