package com.mazalearn.scienceengine;

import java.util.Locale;
import java.util.ResourceBundle;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;

public class Messages extends BasicMessages {
  private static final String BUNDLE_NAME = "com.mazalearn.scienceengine.data.Messages"; //$NON-NLS-1$

  private Locale locale = new Locale("en");
  private Platform platform;
  private ResourceBundle resourceBundle = 
      ResourceBundle.getBundle(BUNDLE_NAME, locale);
  
  public Messages(Platform platform) {
    this.platform = platform;
  }

  public String getString(String key) {
    try {
      String val = resourceBundle.getString(key);
      return platform == Platform.Android ? val : new String(val.getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {
      if (ScienceEngine.DEV_MODE.isDebug()) e.printStackTrace();
      return '!' + key + '!';
    }
  }
  
  public String getLanguage() {
    return locale.getLanguage();
  }

  public void setLanguage(Skin skin, String language) {
    locale = new Locale(language);
    resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    setFont(skin);
  }
}
