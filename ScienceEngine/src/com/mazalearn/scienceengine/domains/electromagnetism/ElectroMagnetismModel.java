package com.mazalearn.scienceengine.domains.electromagnetism;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Ammeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentSource;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Drawing;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Dynamo;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ElectroMagnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.HorseshoeMagnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ScienceTrain;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Wire;

public class ElectroMagnetismModel extends AbstractScience2DModel {
    
  public ElectroMagnetismModel() {
    super();    
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
    case Ammeter: return new Ammeter(x, y, rotation);
    case FieldMeter: return new FieldMeter(x, y, rotation);
    case Magnet: return new Magnet(x, y, rotation);
    case BarMagnet: return new BarMagnet(x, y, rotation);
    case HorseshoeMagnet: return new HorseshoeMagnet(x, y, rotation);
    case CurrentSource: return new CurrentSource(x, y, rotation);
    case ElectroMagnet: return new ElectroMagnet(x, y, rotation);
    case PickupCoil: return new PickupCoil(x, y, rotation, 2E7f);
    case Lightbulb: return new Lightbulb(x, y, rotation);
    case Wire: return new Wire(x, y, rotation);
    case Dynamo: return new Dynamo(x, y, rotation);
    case CurrentCoil: return new CurrentCoil(x, y, rotation);
    case Compass: return new Compass(x, y, rotation);
    case Drawing: return new Drawing(x, y, rotation);
    case ScienceTrain: return new ScienceTrain(x, y, rotation);
    }
    return null;
  }

  @Override
  protected void singleStep() {
    float dt = Gdx.app.getGraphics().getDeltaTime();
    box2DWorld.step(dt, 3, 3);
    propagateField();
    for (Science2DBody body: bodies) {
      if (body.isActive()) {
        body.singleStep(dt);
      }
    }
  }
  
  @Override
  public void initializeConfigs(List<IModelConfig<?>> modelConfigs) {
  }
  
}