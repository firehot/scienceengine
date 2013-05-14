package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;

public class AboutDialog extends Dialog {
  
  public AboutDialog(Skin skin) {
    super("About", skin);
    
    Image image = new Image(new Texture("images/splash.jpg"));
    getContentTable().add(image)
        .width(ScreenComponent.getScaledX(100))
        .height(ScreenComponent.getScaledY(80)).center();
    getContentTable().row();
    
    Label name = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Name"), skin);
    name.setWidth(ScreenComponent.getScaledX(600));

    Label description = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Maza"), skin);
    description.setWidth(ScreenComponent.getScaledX(600));
    description.setAlignment(Align.left, Align.left);
    description.setWrap(true);
    
    // TODO: clean up all copyrights statements
    final Label copyrights = 
        new Label(ScienceEngine.getMsg().getString("ScienceEngine.Copyrights"), skin);
    copyrights.setWidth(ScreenComponent.getScaledX(600));
    copyrights.setAlignment(Align.left, Align.left);
    copyrights.setWrap(true);
    ScrollPane copyrightsPane = new ScrollPane(copyrights, skin);

    getContentTable().add(name).pad(10);
    getContentTable().row();
    getContentTable().add(description).fill().width(ScreenComponent.getScaledX(600));
    getContentTable().row();
    getContentTable().add(copyrightsPane).pad(10).fill()
        .width(ScreenComponent.getScaledX(600))
        .height(ScreenComponent.getScaledY(150));

    Button b = new TextButton("OK", skin);
    this.getButtonTable().add(b);
  }
}