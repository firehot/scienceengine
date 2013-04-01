package com.mazalearn.scienceengine.tutor;

import java.io.IOException;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;

public class DrawingTest {

  public void suppressTestDrawPng() {
    Pixmap snapshot;
    try {
      Profile profile = ScienceEngine.getPreferencesManager().getProfile();
      byte[] bytes = profile.getCoachPixmap();
      snapshot = new Pixmap(new Gdx2DPixmap(bytes, 0, bytes.length, 0));
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not load");
    }
    Image image = new Image(new Texture(snapshot));
  }
}
