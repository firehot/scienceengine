package com.mazalearn.scienceengine.data;

import java.util.MissingResourceException;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.google.gwt.core.client.GWT;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter.Platform;

public class GwtMessages implements IMessage {

  private String language;
  private Platform platform;
  private Messages messages;

  public GwtMessages(Platform platform) {
    this.platform = platform;
    this.messages = (Messages) GWT.create(Messages.class);
  }
  
  @Override
  public String getString(String msg) {
   try {
     return messages.getString(msg);
   } catch (MissingResourceException e) {
     int pos = msg.indexOf(".");
     return msg.substring(pos + 1);
   }
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public void setLanguage(Skin skin, String language) {
    this.language = language;
  }

  @Override
  public void setFont(Skin skin) {
    // TODO Auto-generated method stub
    
  }
}