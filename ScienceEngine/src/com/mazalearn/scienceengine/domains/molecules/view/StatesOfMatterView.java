package com.mazalearn.scienceengine.domains.molecules.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.ColorPanel;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldDirectionProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldMagnitudeProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.VariablesProber;
import com.mazalearn.scienceengine.domains.molecules.model.IMolecularModel;
import com.mazalearn.scienceengine.guru.AbstractScience2DProber;
import com.mazalearn.scienceengine.guru.Guru;

/**
 * States of Matter experiment View.
 */
public class StatesOfMatterView extends AbstractScience2DView {
   
  private IMolecularModel molecularModel;
  private int N;

  public StatesOfMatterView(IMolecularModel molecularModel,
      int width, int height, int N, Skin skin) {
    super(molecularModel, width, height, skin);

    this.N = N;
    this.molecularModel = molecularModel;
  }

  @Override
  protected Actor createActor(Science2DBody body) {
    return null;
  }

  @Override
  protected Actor createActor(String type) {
    int boxWidth = 20;
    int boxHeight = 20;
    if (type.equals("MoleculeBox")) {
      return new MoleculeBox(molecularModel, N, boxWidth, boxHeight, getFont());
    } else if (type.equals("ColorPanel")) {
      return new ColorPanel(type);
    }
    return null;
  }
  
  @Override
  public AbstractScience2DProber createProber(String name, Guru guru, String type) {
    return super.createProber(name, guru, type);
  }  
}
