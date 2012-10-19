package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentCoil;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentSource;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.ElectroMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Wire;

public class ElectroMagnetismModel extends AbstractScience2DModel {
  private RevoluteJointDef coilRotationJointDef = new RevoluteJointDef();
  private Joint coilRotationJoint;
  private Science2DBody currentCoil;
    
  public ElectroMagnetismModel() {
    super();
    
    addBody(createScience2DBody(ComponentType.FieldMeter, 10, 5, 0));
    addBody(createScience2DBody(ComponentType.BarMagnet, 10, 12, 0));
    addBody(createScience2DBody(ComponentType.FieldMagnet, 8, 8, 0));
    addBody(createScience2DBody(ComponentType.FieldMagnet, 12, 8, 0));
    addBody(createScience2DBody(ComponentType.CurrentSource, 12, 14, 0));
    addBody(createScience2DBody(ComponentType.CurrentSource, 10, 12, 0));
    addBody(createScience2DBody(ComponentType.ElectroMagnet, 10, 12, 0));
    addBody(createScience2DBody(ComponentType.PickupCoil, 23, -4, 0));
    addBody(createScience2DBody(ComponentType.Lightbulb, 23, 25, 0));
    addBody(createScience2DBody(ComponentType.Wire, 8, 12, 0));
    addBody(createScience2DBody(ComponentType.Wire, 16, 12, 0));
    addBody(createScience2DBody(ComponentType.CurrentCoil, 43, 28, 0));
    addBody(createScience2DBody(ComponentType.Compass, 0, 5, 0));
    
    currentCoil = findBody(ComponentType.CurrentCoil);
    
    for (Science2DBody body: bodies) {
      body.initializeConfigs();
    }
    reset();
  }
  
  private Science2DBody createScience2DBody(ComponentType componentType, 
      float x, float y, float rotation) {
    switch(componentType) {
    case FieldMeter: return new FieldMeter(x, y, rotation);
    case BarMagnet: return new BarMagnet(x, y, rotation);
    case FieldMagnet: return new FieldMagnet(x, y, rotation);
    case CurrentSource: return new CurrentSource(x, y, rotation);
    case ElectroMagnet: return new ElectroMagnet(x, y, rotation);
    case PickupCoil: return new PickupCoil(x, y, rotation, 2E7f);
    case Lightbulb: return new Lightbulb(x, y, rotation);
    case Wire: return new Wire(x, y, rotation);
    case CurrentCoil: return new CurrentCoil(x, y, rotation);
    case Compass: return new Compass(x, y, rotation);
    }
    return null;
  }

  @Override
  public void initializeConfigs(List<IModelConfig<?>> modelConfigs) {
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
  public void reset() {
    for (Science2DBody body: bodies) {
      body.resetInitial();
    }
    if (coilRotationJoint == null && currentCoil != null) {
      coilRotationJointDef.initialize(currentCoil.getBody(), Science2DBody.getGround(), 
          currentCoil.getWorldPoint(Vector2.Zero));
      coilRotationJoint = box2DWorld.createJoint(coilRotationJointDef);
    }
  }

  public List<Science2DBody> getBodies() {
    return bodies;
  }
}