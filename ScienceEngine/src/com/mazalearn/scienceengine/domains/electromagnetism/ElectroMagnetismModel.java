package com.mazalearn.scienceengine.domains.electromagnetism;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Attribute;
import com.mazalearn.scienceengine.domains.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentSource;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ElectroMagnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMagnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.domains.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Wire;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentSource.CurrentType;

public class ElectroMagnetismModel extends AbstractScience2DModel {
  private RevoluteJointDef coilRotationJointDef = new RevoluteJointDef();
  private Joint coilRotationJoint;
  private Science2DBody currentCoil;
  private Lightbulb lightbulb;
    
  public ElectroMagnetismModel() {
    super();    
  }

  @Override
  public void prepareModel() {
    currentCoil = findBody(ComponentType.CurrentCoil);
    
    for (Science2DBody body: bodies) {
      body.initializeConfigs();
    }
    reset();
  }
  
  @Override
  protected Science2DBody createScience2DBody(String componentTypeName, 
      float x, float y, float rotation) {
    
    ComponentType componentType = null;
    try {
      componentType = ComponentType.valueOf(componentTypeName);
    } catch (IllegalArgumentException e) {
      return null;
    }
    
    switch(componentType) {
    case FieldMeter: return new FieldMeter(x, y, rotation);
    case BarMagnet: return new BarMagnet(x, y, rotation);
    case FieldMagnet: return new FieldMagnet(x, y, rotation);
    case CurrentSource: return new CurrentSource(x, y, rotation);
    case ElectroMagnet: return new ElectroMagnet(x, y, rotation);
    case PickupCoil: return new PickupCoil(x, y, rotation, 2E7f);
    case Lightbulb: return lightbulb = new Lightbulb(x, y, rotation);
    case Wire: return new Wire(x, y, rotation);
    case CurrentCoil: return new CurrentCoil(x, y, rotation);
    case Compass: return new Compass(x, y, rotation);
    }
    return null;
  }

  // TODO: Dummy configs - do in a cleaner way
  float[] dummies = new float[10];
  @Override
  public void initializeConfigs(List<IModelConfig<?>> modelConfigs) {
    modelConfigs.add(new AbstractModelConfig<Float>(null, 
        Attribute.AirPermittivity, 0f, 4f) {
      public Float getValue() { return dummies[0]; }
      public void setValue(Float value) { dummies[0] = value; }
     public boolean isPossible() { return lightbulb != null; }
    });
    
    modelConfigs.add(new AbstractModelConfig<Float>(null, 
        Attribute.RoomTemperature, 0f, 4f) {
      public Float getValue() { return dummies[1]; }
      public void setValue(Float value) { dummies[1] = value; }
      public boolean isPossible() { return lightbulb != null; }
    });
    
    modelConfigs.add(new AbstractModelConfig<Float>(null, 
        Attribute.EarthMagneticField, 0f, 4f) {
      public Float getValue() { return dummies[2]; }
      public void setValue(Float value) { dummies[2] = value; }
      public boolean isPossible() { return lightbulb != null; }
    });
    
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