package com.mazalearn.scienceengine;

import java.io.File;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public class AbstractPlatformAdapter implements PlatformAdapter {

  private static final float DEFAULT_FONT_SIZE = 15f;
  private IMessage message;

  @Override
  public void browseURL(String url) {
  }

  @Override
  public void showURL(String uri) {
    FileHandle file = Gdx.files.external(uri);
    if (file.exists()) {
      String path = file.file().getAbsolutePath();
      browseURL("file:///" + path.replace("\\", "/"));
    }
  }

  @Override
  public boolean playVideo(File file) {
    return false;
  }

  @Override
  public Stage createLevelEditor(IScience2DController science2DController,
      AbstractScreen screen) {
    return (Stage) science2DController.getView();
  }

  @Override
  public IMessage getMsg() {
    if (message == null) {
      message = new IMessage() {
        @Override
        public String getString(String msg) {
          int pos = msg.indexOf(".");
          return msg.substring(pos + 1);
        }

        @Override
        public String getLanguage() {
          return "en";
        }

        @Override
        public void setLanguage(Skin skin, String language) {
        }

        @Override
        public void setFont(Skin skin) {
        }
      };
    }
    return message;
  }

  @Override
  public Platform getPlatform() {
    return Platform.IOS;
  }

  @Override
  public BitmapFont getFont(int pointSize) {
    FileHandle skinFile = Gdx.files.internal("skin/uiskin.json");
    Skin  skin = new Skin(skinFile);
    skin.add("en", skin.getFont("default-font"));
    getMsg().setFont(skin);
    BitmapFont font = skin.getFont("default-font");
    font.setScale(pointSize / DEFAULT_FONT_SIZE);
    return font;
  }

}
