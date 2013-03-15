package com.mazalearn.gwt.server;

public class Base64 {
  private final static String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

  public static byte[] decode(String s) {

    // remove/ignore any characters not in the base64 characters list
    // or the pad character -- particularly newlines
    s = s.replaceAll("[^" + base64chars + "=]", "");

    // replace any incoming padding with a zero pad (the 'A' character is
    // zero)
    String p = (s.charAt(s.length() - 1) == '=' ? (s.charAt(s.length() - 2) == '=' ? "AA"
        : "A")
        : "");

    s = s.substring(0, s.length() - p.length()) + p;
    int resLength = (int) Math.ceil(((float) (s.length()) / 4f) * 3f);
    byte[] bufIn = new byte[resLength];
    int bufIn_i = 0;

    // increment over the length of this encrypted string, four characters
    // at a time
    for (int c = 0; c < s.length(); c += 4) {

      // each of these four characters represents a 6-bit index in the
      // base64 characters list which, when concatenated, will give the
      // 24-bit number for the original 3 characters
      int n = (base64chars.indexOf(s.charAt(c)) << 18)
          + (base64chars.indexOf(s.charAt(c + 1)) << 12)
          + (base64chars.indexOf(s.charAt(c + 2)) << 6)
          + base64chars.indexOf(s.charAt(c + 3));

      // split the 24-bit number into the original three 8-bit (ASCII)
      // characters

      char c1 = (char) ((n >>> 16) & 0xFF);
      char c2 = (char) ((n >>>8) & 0xFF);
      char c3 = (char) (n & 0xFF);

      bufIn[bufIn_i++] = (byte) c1;
      bufIn[bufIn_i++] = (byte) c2;
      bufIn[bufIn_i++] = (byte) c3;

    }

    return bufIn;
  }
  
  static final char[] charTab = 
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray (); 

  /**
   * @param string
   * @return the input string encoded using Base 64 encoding.
   */
  public static String encode (String string) {
      return encode(string.getBytes()).toString ();
  }
  
  public static String encode (byte [] data) {
      return encode (data, 0, data.length, null).toString ();
  }


  /** Encodes the part of the given byte array denoted by start and
      len to the Base64 format.  The encoded data is appended to the
      given StringBuffer. If no StringBuffer is given, a new one is
      created automatically. The StringBuffer is the return value of
      this method. */


  public static StringBuffer encode (byte [] data, int start, int len, StringBuffer buf) {

      if (buf == null) 
          buf = new StringBuffer (data.length * 3 / 2);

      int end = len - 3;
      int i = start;
      int n = 0;

      while (i <= end) {
          int d = (((data [i]) & 0x0ff) << 16) 
              | (((data [i+1]) & 0x0ff) << 8)
              | ((data [i+2]) & 0x0ff);

          buf.append (charTab [(d >> 18) & 63]);
          buf.append (charTab [(d >> 12) & 63]);
          buf.append (charTab [(d >> 6) & 63]);
          buf.append (charTab [d & 63]);

          i += 3;

          if (n++ >= 14) {
              n = 0;
              buf.append ("\r\n");
          }
      }


      if (i == start + len - 2) {
          int d = (((data [i]) & 0x0ff) << 16) 
              | (((data [i+1]) & 255) << 8);

          buf.append (charTab [(d >> 18) & 63]);
          buf.append (charTab [(d >> 12) & 63]);
          buf.append (charTab [(d >> 6) & 63]);
          buf.append ("=");
      }
      else if (i == start + len - 1) {
          int d = ((data [i]) & 0x0ff) << 16;

          buf.append (charTab [(d >> 18) & 63]);
          buf.append (charTab [(d >> 12) & 63]);
          buf.append ("==");
      }

      return buf;
  }

}