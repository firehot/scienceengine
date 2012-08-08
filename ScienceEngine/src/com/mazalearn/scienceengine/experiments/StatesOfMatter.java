package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
    defaults().fill();
    add(new TextButton("top", skin)).colspan(3);
    row();
    add(new TextButton("left", skin));
    add(new MoleculeBox()).expand().fill();
    add(new TextButton("right", skin));
    row();
    add(new TextButton("bottom", skin)).colspan(3);
    row();
  }
}
