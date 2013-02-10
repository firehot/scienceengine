package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScreenComponent;

public class ChallengeEndDialog extends Dialog {
  
  public ChallengeEndDialog(final Stage stage, Skin skin, String title, String contents) {
    super("\n\n" + title, skin);
    
    setBackground(createBackground());
    
    Label description = new Label("\n\n" + contents, skin);
    description.setWidth(400);
    description.setWrap(true);
    
    getContentTable().add(description).width(400).pad(10);

    Button b = new TextButton("OK", skin);
    this.getButtonTable().add(b).width(200);
  }

  private static TextureRegionDrawable createBackground() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    Color c = Color.LIGHT_GRAY;
    pixmap.setColor(c.r, c.g, c.b, 0.6f);
    pixmap.fillRectangle(0, 0, 1, 1);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap), 
        ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    pixmap.dispose();
    return new TextureRegionDrawable(textureRegion);
  }
}