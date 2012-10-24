package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public interface IMessage {
  String getString(String msg);
  String getLanguage();
  void setLanguage(Skin skin, String language);
  void setFont(Skin skin);
}
