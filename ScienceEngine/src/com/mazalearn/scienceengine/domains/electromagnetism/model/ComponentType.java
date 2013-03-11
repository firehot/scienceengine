package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ComponentType implements IComponentType {
  BarMagnet("barmagnet"),
  CurrentSource("currentsource"),
  Compass("compass"),
  ElectroMagnet("electromagnet-base"),
  Lightbulb("lightbulb"),
  FieldMeter("arrow"),
  PickupCoil("coppercoils-front1"),
  Wire("currentwire-up"),
  Ammeter("ammeter"), 
  HorseshoeMagnet("horseshoemagnet"),
  CurrentCoil("currentcoil_nocommutator"), 
  Drawing("draw"), 
  ScienceTrain("engine"), 
  Dynamo("copperwire"),
  Magnet("neodymium"), 
  Monopole("northpole");
  
  private String textureName;
  
  private ComponentType(String textureFilename) {
    this.textureName = textureFilename;
  }
  
  public String getTextureName() {
    return textureName;
  }
  
  public String toString() {
    return ScienceEngine.getMsg().getString("Name." + name());  
  }
  
  public static ComponentType valueOf(IComponentType cType) {
    for (ComponentType componentType: ComponentType.values()) {
      if (componentType.name().equals(cType.name())) {
        return componentType;
      }
    }
    return null;
  }
}
