package com.mazalearn.scienceengine.app.services;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
  private static final String BUNDLE_NAME = "com.mazalearn.scienceengine.app.services.data.messages"; //$NON-NLS-1$

  private static Locale LOCALE = new Locale("hi");
  private static ResourceBundle RESOURCE_BUNDLE = 
      ResourceBundle.getBundle(BUNDLE_NAME, LOCALE);


  private Messages() {
  }

  public static String getString(String key) {
    try {
      String val = RESOURCE_BUNDLE.getString(key);
      return new String(val.getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
      return '!' + key + '!';
    }
  }
  
  public static Locale getLocale() {
    return LOCALE;
  }

  public static void setLocale(Locale locale) {
    LOCALE = locale;
    RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, LOCALE);
  }
}
