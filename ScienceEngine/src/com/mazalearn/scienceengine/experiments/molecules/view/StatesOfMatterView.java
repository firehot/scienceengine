package com.mazalearn.scienceengine.experiments.molecules.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.molecules.model.IMolecularModel;
import com.mazalearn.scienceengine.view.AbstractExperimentView;
import com.mazalearn.scienceengine.view.ColorPanel;

/**
 * States of Matter experiment View.
 */
public class StatesOfMatterView extends AbstractExperimentView {
  
  private final Table layoutTable;
  private final IMolecularModel molecularModel;
  
  public StatesOfMatterView(IMolecularModel molecularModel, int boxWidth, int boxHeight, int N) {
    super(molecularModel);
    this.molecularModel = molecularModel;

    layoutTable = new Table();
    layoutTable.setFillParent(true);
    if (ScienceEngine.DEV_MODE != ScienceEngine.DevMode.PRODUCTION) {
      layoutTable.debug();
    }
    // Ceiling of box
    layoutTable.add(new ColorPanel()).fill().colspan(3).height(10);
    layoutTable.row();
    // Sides and box
    layoutTable.add(new ColorPanel()).fill().width(10);
    layoutTable.add(new MoleculeBox(molecularModel, N, boxWidth, boxHeight, PIXELS_PER_M)).expand().fill();
    layoutTable.add(new ColorPanel()).fill().width(10);
    layoutTable.row();
    // Bottom of box
    layoutTable.add(new ColorPanel()).fill().colspan(3).height(30); // ??? TODO why is this 30???
    layoutTable.row();
    this.addComponent("Molecule Box", layoutTable);    
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    if (!isPaused) {
      molecularModel.simulateSteps(10);
    }
  }
}
