package com.mazalearn.scienceengine.experiments.molecules.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;
import com.mazalearn.scienceengine.core.view.ColorPanel;
import com.mazalearn.scienceengine.experiments.molecules.model.IMolecularModel;

/**
 * States of Matter experiment View.
 */
public class StatesOfMatterView extends AbstractScience2DStage {
   
  public StatesOfMatterView(IMolecularModel molecularModel,
      int width, int height, int N, Skin skin) {
    super(molecularModel, width, height, skin);

    // Ceiling of box
    Actor ceiling = new ColorPanel();
    ceiling.setName("Ceiling");
    // Sides and box
    Actor leftSide = new ColorPanel();
    leftSide.setName("LeftSide");
    int boxWidth = 20;
    int boxHeight = 20;
    Actor moleculeBox = new MoleculeBox(molecularModel, N, boxWidth, boxHeight, ScienceEngine.PIXELS_PER_M);
    moleculeBox.setName("MoleculeBox");
    Actor rightSide = new ColorPanel();
    rightSide.setName("RightSide");
    Actor floor = new ColorPanel();
    floor.setName("Floor");
    
    this.addActor(ceiling);
    this.addActor(leftSide);
    this.addActor(moleculeBox);
    this.addActor(rightSide);
    this.addActor(floor);
  }  
}
