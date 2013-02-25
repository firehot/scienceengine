package com.mazalearn.scienceengine;

import java.io.File;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public class AbstractPlatformAdapter implements IPlatformAdapter {

  private static final float DEFAULT_FONT_SIZE = 15f;
  protected IMessage messages;
  private Platform platform;

  public AbstractPlatformAdapter(Platform platform) {
    this.platform = platform;
  }
  
  @Override
  public void browseURL(String url) {
    Gdx.net.openURI(url);
  }

  @Override
  public void showExternalURL(String uri) {
    showFileUri(Gdx.files.external(uri));
  }

  private void showFileUri(FileHandle file) {
    if (file.exists()) {
      String path = file.file().getAbsolutePath();
      browseURL("file:///" + path.replace("\\", "/"));
    }
  }

  @Override
  public void showInternalURL(String uri) {
    showFileUri(Gdx.files.internal(uri));
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
    if (messages == null) {
      messages = new BasicMessages();
    }
    return messages;
  }

  @Override
  public Platform getPlatform() {
    return platform;
  }

  @Override
  public BitmapFont getScaledFont(int pointSize) {
    FileHandle skinFile = Gdx.files.internal("skin/uiskin.json");
    Skin  skin = new Skin(skinFile);
    skin.add("en", skin.getFont("default-font"));
    getMsg().setFont(skin);
    BitmapFont font = skin.getFont("default-font");
    font.setScale(pointSize / DEFAULT_FONT_SIZE);
    return font;
  }

  @Override
  public BitmapFont loadFont(Skin skin, String language) {
    return skin.getFont("en");
  }

  @Override
  public void getBytes(Pixmap pixmap, byte[] lines) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void putBytes(Pixmap pixmap, byte[] lines) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public byte[] getPngBytes(Pixmap snapshot) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean supportsLanguage() {
    return false;
  }

  @Override
  public void httpPost(String path, String contentType, Map<String, String> params,
      byte[] data){
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void takeSnapshot(Stage stage, String topic, int level, int x, int y, int width, int height) {
  }
}
