package com.badlogic.gdx.graphics.g2d.harfbuzz;

public class ComplexScriptLayout {
  
  private String language;
  private String fontFilePath;

  private native int[] jniGetGlyphsForText(String unicodeText);
  
  // Initialize MUST be called before using for layout.
  // This means setLanguage must be called on the class before using.
  private synchronized native void jniInitialize(String fontFilePath, String language); 

  static {
    System.loadLibrary("complex_script_layout");
  }
  
  public int[] getGlyphsAfterShaping(CharSequence unicodeText, int start, int end) {
    int[] glyphs = jniGetGlyphsForText(unicodeText.toString().substring(start, end));
    return glyphs;
  }

  public void setLanguage(String language, String fontFileName) {
    this.fontFilePath = "/sdcard/data/" + fontFileName;
    if (language.equals("ka")) {
      this.language = "Knda";
    } else if (language.equals("hi")) {
      this.language = "Deva";      
    } else if (language.equals("ta")) {
      this.language = "Taml";
    }
    jniInitialize(this.fontFilePath, this.language);
  }
}
