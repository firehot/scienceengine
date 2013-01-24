package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mazalearn.scienceengine.ScienceEngine;

public class AboutDialog extends Dialog {
  
  public AboutDialog(Skin skin) {
    super("About", skin);
    
    getContentTable().debug();
    //setBackground(createBackground());
    Image image = new Image(new Texture("image-atlases/splash.jpg"));
    getContentTable().add(image).width(100).height(80).center();
    getContentTable().row();
    
    Label description = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Maza"), skin);
    description.setWidth(600);
    description.setWrap(true);
    
    final Label copyrights = 
        new Label(ScienceEngine.getMsg().getString("ScienceEngine.Copyrights"), skin);
    copyrights.setWidth(600);
    copyrights.setWrap(true);

    getContentTable().add(description).width(600).pad(10);
    getContentTable().row();
    getContentTable().add(copyrights).width(600).pad(10);

    Button b = new TextButton("OK", skin);
    this.getButtonTable().add(b).width(200);
  }
}