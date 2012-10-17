package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ComponentType implements IComponentType {
  BarMagnet("images/barmagnet-pivoted.png"),
  CurrentSource("images/currentsource.png"),
  Compass("images/compass.png"),
  ElectroMagnet("images/electromagnet-base.png"),
  Lightbulb("images/lightbulb.png"),
  FieldMeter("images/arrow.png"),
  PickupCoil("images/coppercoils-front1.png"),
  Wire("images/currentwire-up.png"),
  Voltmeter(""), 
  FieldMagnet("images/simplemagnetsn.png"),
  CurrentCoil("images/currentcoil_nocommutator.png");
  
  private String textureFilename;
  
  private ComponentType(String textureFilename) {
    this.textureFilename = textureFilename;
  }
  
  public String getTextureFilename() {
    return textureFilename;
  }
  
  // TODO: localize this
  public String toString() {
    return name();
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
