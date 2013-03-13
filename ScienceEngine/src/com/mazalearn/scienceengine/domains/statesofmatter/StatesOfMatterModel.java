package com.mazalearn.scienceengine.domains.statesofmatter;

import java.util.List;
import java.util.Map;

import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.statesofmatter.model.AbstractMoleculeBox;
import com.mazalearn.scienceengine.domains.statesofmatter.model.ComponentType;
import com.mazalearn.scienceengine.domains.statesofmatter.model.LJMoleculeBox;

public class StatesOfMatterModel extends AbstractScience2DModel {

  private static final int BOX_HEIGHT = 20;
  private static final int BOX_WIDTH = 20;
  protected int N = 200; // Number of molecules
  private AbstractMoleculeBox moleculeBox;

  public StatesOfMatterModel(int N) {
    this.N = N;
    this.numStepsPerView = 10;
  }
  
  @Override
  public void simulateSteps(float delta) {
    super.simulateSteps(delta);
    moleculeBox.reScaleDt();
  }

  @Override
  protected void singleStep() {
    moleculeBox.singleStep();
  }

  @Override
  public void initializeConfigs(Map<String, IModelConfig<?>> modelConfigs) {
  }

  @Override
  protected Science2DBody createScience2DBody(String componentTypeName,
      float x, float y, float rotation) {
    ComponentType componentType = null;
    try {
      componentType = ComponentType.valueOf(componentTypeName);
    } catch (IllegalArgumentException e) {
      return super.createScience2DBody(componentTypeName, x, y, rotation);
    }
    
    switch(componentType) {
    case MoleculeBox: 
      moleculeBox = new LJMoleculeBox(BOX_WIDTH, BOX_HEIGHT, N, 0.5);
      return moleculeBox;
    }
    return null;
  }
}
