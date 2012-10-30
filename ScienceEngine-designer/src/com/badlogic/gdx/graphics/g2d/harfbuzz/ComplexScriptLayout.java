package com.badlogic.gdx.graphics.g2d.harfbuzz;

public class ComplexScriptLayout {
  
  private static String fontLanguage, loadedLanguage;
  private static String fontFilePath;

  private native int[] jniGetGlyphsForText(String unicodeText);
  
  // Initialize MUST be called before using for layout.
  // This means setLanguage must be called on the class before using.
  private static synchronized native void jniInitialize(String fontFilePath, String language); 

  static {
    System.loadLibrary("complex_script_layout");
  }
  
  public int[] getGlyphsAfterShaping(CharSequence unicodeText, int start, int end) {
    int[] glyphs = jniGetGlyphsForText(unicodeText.toString().substring(start, end));
    return glyphs;
  }

  public synchronized static void setLanguage(String language, String fontFileName) {
    if (language.equals(loadedLanguage)) return;
    fontFilePath = "/sdcard/data/" + fontFileName;
    if (language.equals("ka")) {
      fontLanguage = "Knda";
    } else if (language.equals("hi")) {
      fontLanguage = "Deva";      
    } else if (language.equals("ta")) {
      fontLanguage = "Taml";
    }
    jniInitialize(fontFilePath, fontLanguage);
    loadedLanguage = language;
  }
}
