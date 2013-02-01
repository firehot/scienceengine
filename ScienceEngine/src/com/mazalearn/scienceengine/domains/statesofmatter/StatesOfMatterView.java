package com.mazalearn.scienceengine.domains.statesofmatter;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;

/**
 * States of Matter domain View.
 */
public class StatesOfMatterView extends AbstractScience2DView {
   
  public StatesOfMatterView(IScience2DModel statesOfMatterModel,
      int width, int height, int N, Skin skin, IScience2DController controller) {
    super(statesOfMatterModel, width, height, skin, controller);
  }

}
