package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.model.molecule.IMolecularModel.Heating;
import com.mazalearn.scienceengine.experiments.view.ColorPanel;
import com.mazalearn.scienceengine.experiments.view.StatesOfMatterView;

/**
 * States of Matter iExperimentModel
 */
public class StatesOfMatterExperiment extends Table {
  private int temperatureLevel = 0;
  private StatesOfMatterView statesOfMatterView;
  Table buttonTable;
  
  public StatesOfMatterExperiment(Skin skin) {
    super(skin);
    buttonTable = new Table(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
      buttonTable.debug();
    }
    // Row of buttons for the states
    buttonTable.add(createStateButton(skin, "Solid", 0.2)).expand().fill();
    buttonTable.add(createStateButton(skin, "Liqid", 0.95)).expand().fill();
    buttonTable.add(createStateButton(skin, "Gas", 5)).expand().fill();
    add(buttonTable).fill().colspan(3);   
    row();
    // Ceiling of box
    add(new ColorPanel()).fill().colspan(3).height(10);
    row();
    // Sides and box
    add(new ColorPanel()).fill().width(10);
    statesOfMatterView = new StatesOfMatterView();
    add(statesOfMatterView).expand().fill();
    add(new ColorPanel()).fill().width(10);
    row();
    // Floor of box - also controls heating.
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);

    final TextureRegion[] textureRegion = {
        new TextureRegion(new Texture(pixmap)),
        new TextureRegion(new Texture("images/iceberg.jpg")),
        new TextureRegion(new Texture("images/fire-texture.jpg")),            
    };
    pixmap.dispose();
    final Image heatingControl = new Image(textureRegion[0], Scaling.stretch);
    //new TextButton("", skin);
    heatingControl.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        temperatureLevel = (temperatureLevel + 1) % 3;
        Heating tLevel = Heating.NEUTRAL;
        switch (temperatureLevel) {
        case 0: tLevel = Heating.NEUTRAL; break;
        case 1: tLevel = Heating.COLD; break;
        case 2: tLevel = Heating.HOT; break;
        }
        statesOfMatterView.setHeating(tLevel);
        heatingControl.setRegion(textureRegion[temperatureLevel]);
      }
    });
    add(heatingControl).fill().colspan(3).height(30);
    row();
  }

  private TextButton createStateButton(Skin skin, String caption, 
      final double temperature) {
    TextButton textButton = new TextButton(caption, skin);
    textButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        statesOfMatterView.setTemperature(temperature);
      }
    });
    return textButton;
  }
}
