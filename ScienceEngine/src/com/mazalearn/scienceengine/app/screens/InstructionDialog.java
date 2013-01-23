package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.esotericsoftware.tablelayout.Cell;

public class InstructionDialog extends Dialog {
  
  public InstructionDialog(final Stage stage, Skin skin, String title, 
      String contents, String instructions, String buttonText) {
    super("\n\n" + title, skin);
    
    setBackground(createBackground());
    this.setSize(AbstractScreen.VIEWPORT_WIDTH, AbstractScreen.VIEWPORT_HEIGHT);
    
    Label description = new Label("\n\n" + contents, skin);
    description.setWidth(400);
    description.setWrap(true);
    
    final Label navigation = new Label(instructions, skin);
    navigation.setWidth(400);
    navigation.setWrap(true);

    final TextButton navigationButton = new TextButton("Instructions >>", skin);

    getContentTable().add(description).width(400).pad(10);
    getContentTable().row();
    getContentTable().add(navigationButton);
    getContentTable().row();
    @SuppressWarnings("unchecked")
    final Cell<Label> navCell = (Cell<Label>) getContentTable().add(navigation).width(400).pad(10);
    getContentTable().setHeight(400);
    getContentTable().row();
    navCell.setWidget(null);

    navigationButton.addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        if (navCell.getWidget() != null) {
          navCell.setWidget(null);
          navigationButton.setText("Instructions >>");
        } else {
          navCell.setWidget(navigation);
          navigationButton.setText("Instructions <<");
        }
        hide();
        show(stage);
      }
    });

    Button b = new TextButton(buttonText, skin);
    this.getButtonTable().add(b).width(200);
  }

  private static TextureRegionDrawable createBackground() {
    Pixmap pixmap = new Pixmap(512, 256, Pixmap.Format.RGBA8888);
    Color c = Color.LIGHT_GRAY;
    pixmap.setColor(c.r, c.g, c.b, 0.6f);
    pixmap.fillRectangle(0, 0, 512, 256);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return new TextureRegionDrawable(textureRegion);
  }
}