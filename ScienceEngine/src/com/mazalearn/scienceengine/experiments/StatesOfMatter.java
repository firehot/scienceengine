package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * States of Matter experiment
 */
public class StatesOfMatter extends Table {
  
  public StatesOfMatter(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    
    add(new StaticText()).fill().colspan(3).height(10);
    row();
    add(new StaticText()).fill().width(10);
    add(new MoleculeBox()).expand().fill();
    add(new StaticText()).fill().width(10);
    row();
    add(new StaticText()).fill().colspan(3).height(10);
    row();
  }
}
