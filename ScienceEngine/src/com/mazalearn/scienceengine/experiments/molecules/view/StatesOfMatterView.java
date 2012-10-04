package com.mazalearn.scienceengine.experiments.molecules.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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

    int boxWidth = 20;
    int boxHeight = 20;
    Actor moleculeBox = new MoleculeBox(molecularModel, N, boxWidth, boxHeight);
    this.addActor(new ColorPanel("Ceiling"));
    this.addActor(new ColorPanel("LeftSide"));
    this.addActor(moleculeBox);
    this.addActor(new ColorPanel("RightSide"));
    this.addActor(new ColorPanel("Floor"));
  }  
}
