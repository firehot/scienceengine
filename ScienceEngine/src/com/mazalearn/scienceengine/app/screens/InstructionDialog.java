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

public class InstructionDialog extends Dialog {
  
  public InstructionDialog(final Stage stage, Skin skin, String title, 
      String contents, String instructions, String buttonText) {
    super("\n\n" + title, skin);
    
    setBackground(createBackground());
    
    Label description = new Label("\n\n" + contents, skin);
    description.setWidth(400);
    description.setWrap(true);
    
    final Label navigation = new Label(instructions, skin);
    navigation.setWidth(400);
    navigation.setWrap(true);

    getContentTable().add(description).width(400).pad(10);

    Button b = new TextButton(buttonText, skin);
    this.getButtonTable().add(b).width(200);
  }

  private static TextureRegionDrawable createBackground() {
    Pixmap pixmap = new Pixmap(512, 256, Pixmap.Format.RGBA8888);
    Color c = Color.LIGHT_GRAY;
    pixmap.setColor(c.r, c.g, c.b, 0.6f);
    pixmap.fillRectangle(0, 0, 512, 256);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap), 
        ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    pixmap.dispose();
    return new TextureRegionDrawable(textureRegion);
  }
}