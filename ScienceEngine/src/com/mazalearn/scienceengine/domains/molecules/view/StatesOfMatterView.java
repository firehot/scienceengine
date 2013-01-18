package com.mazalearn.scienceengine.domains.molecules.view;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.domains.molecules.model.IMolecularModel;

/**
 * States of Matter experiment View.
 */
public class StatesOfMatterView extends AbstractScience2DView {
   
  public StatesOfMatterView(IMolecularModel molecularModel,
      int width, int height, int N, Skin skin, IScience2DController controller) {
    super(molecularModel, width, height, skin, controller);
  }

}
