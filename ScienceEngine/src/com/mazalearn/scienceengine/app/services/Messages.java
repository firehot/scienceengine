package com.mazalearn.scienceengine.app.services;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
  private static final String BUNDLE_NAME = "com.mazalearn.scienceengine.app.services.data.messages"; //$NON-NLS-1$

  private static Locale locale = new Locale("en");
  private static ResourceBundle resoourceBundle = 
      ResourceBundle.getBundle(BUNDLE_NAME, locale);


  private Messages() {
  }

  public static String getString(String key) {
    try {
      String val = resoourceBundle.getString(key);
      return new String(val.getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
      return '!' + key + '!';
    }
  }
  
  public static Locale getLocale() {
    return locale;
  }

  public static void setLocale(Locale locale) {
    Messages.locale = locale;
    resoourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }
}
