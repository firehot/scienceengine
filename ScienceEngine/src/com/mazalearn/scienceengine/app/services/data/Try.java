package com.mazalearn.scienceengine.app.services.data;

import java.io.IOException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Try {

  public Try() {}
  
  public void dosomething() throws IOException {
    FileHandle rootFileHandle = Gdx.files.internal("data/messages.properties");  
    FileHandle kannadaFileHandle = Gdx.files.internal("data/messages_ka.properties");  
    final ResourceBundle rootResourceBundle = new PropertyResourceBundle(rootFileHandle.read());  
    ResourceBundle kannadaResourceBundle = new PropertyResourceBundle(kannadaFileHandle.read()) {{  
        setParent(rootResourceBundle);  
    }};
    
    ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale("ka")); 
  }
}
