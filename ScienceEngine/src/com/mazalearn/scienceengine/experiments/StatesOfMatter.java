package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.molecule.MolecularModel.Heating;

/**
 * States of Matter experiment
 */
public class StatesOfMatter extends Table {
  private int temperatureLevel = 0;
  private MoleculeBox moleculeBox;
  Table buttonTable;
  
  public StatesOfMatter(Skin skin) {
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
    moleculeBox = new MoleculeBox();
    add(moleculeBox).expand().fill();
    add(new ColorPanel()).fill().width(10);
    row();
    // Floor of box - also controls heating.
    Button heatingControl = new TextButton("heating", skin);
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
        moleculeBox.setHeating(tLevel);
      }
    });
    add(heatingControl).fill().colspan(3).height(10);
    row();
  }

  private TextButton createStateButton(Skin skin, String caption, 
      final double temperature) {
    TextButton textButton = new TextButton(caption, skin);
    textButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        moleculeBox.setTemperature(temperature);
      }
    });
    return textButton;
  }
}
