package com.mazalearn.scienceengine.experiments.molecules.view;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.Science2DExperimentStage;
import com.mazalearn.scienceengine.core.view.ColorPanel;
import com.mazalearn.scienceengine.experiments.molecules.model.IMolecularModel;

/**
 * States of Matter experiment View.
 */
public class StatesOfMatterView extends Science2DExperimentStage {
  
  private final Table layoutTable;
  private final IMolecularModel molecularModel;
  
  public StatesOfMatterView(IMolecularModel molecularModel,
      int width, int height, int N, Skin skin) {
    super(molecularModel, width, height, skin);
    this.molecularModel = molecularModel;

    layoutTable = new Table("Molecule Box");
    layoutTable.setFillParent(true);
    if (ScienceEngine.DEV_MODE != ScienceEngine.DevMode.PRODUCTION) {
      layoutTable.debug();
    }
    // Ceiling of box
    layoutTable.add(new ColorPanel()).fill().colspan(3).height(10);
    layoutTable.row();
    // Sides and box
    layoutTable.add(new ColorPanel()).fill().width(10);
    int boxWidth = 20;
    int boxHeight = 20;
    layoutTable.add(new MoleculeBox(molecularModel, N, boxWidth, boxHeight, ScienceEngine.PIXELS_PER_M)).expand().fill();
    layoutTable.add(new ColorPanel()).fill().width(10);
    layoutTable.row();
    // Bottom of box
    layoutTable.add(new ColorPanel()).fill().colspan(3).height(30); // ??? TODO why is this 30???
    layoutTable.row();
    this.addActor(layoutTable);    
  }  
}
