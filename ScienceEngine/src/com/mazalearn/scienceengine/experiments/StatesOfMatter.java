package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.molecule.MolecularModel.TemperatureLevel;

/**
 * States of Matter experiment
 */
public class StatesOfMatter extends Table {
  private int temperatureLevel = 0;
  private MoleculeBox moleculeBox;
  
  public StatesOfMatter(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    
    add(new ColorPanel()).fill().colspan(3).height(10);
    row();
    add(new ColorPanel()).fill().width(10);
    moleculeBox = new MoleculeBox();
    add(moleculeBox).expand().fill();
    add(new ColorPanel()).fill().width(10);
    row();
    Button temperatureControl = new Button(skin);
    temperatureControl.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        temperatureLevel = (temperatureLevel + 1) % 3;
        TemperatureLevel tLevel = TemperatureLevel.NEUTRAL;
        switch (temperatureLevel) {
        case 0: tLevel = TemperatureLevel.NEUTRAL; break;
        case 1: tLevel = TemperatureLevel.COLD; break;
        case 2: tLevel = TemperatureLevel.HOT; break;
        }
        moleculeBox.setTemperature(tLevel);
      }
    });
    add(temperatureControl).fill().colspan(3).height(10);
    row();
  }
}
