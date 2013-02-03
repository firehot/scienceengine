package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ScreenComponent implements IComponentType {
  Title(400, 465, Color.WHITE),
  Status(400, 10, Color.WHITE),
  User(730, 465, Color.WHITE),
  BackButton(2, -30, Color.CLEAR), 
  ViewControls(130, -30, Color.CLEAR);
  
  private int x;
  private int y;
  private Color color;

  private ScreenComponent(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }
  
  public int getX() { return x; }
  
  public int getY() { return y; }
  
  public Color getColor() { return color; }
  
  public String toString() {
    return ScienceEngine.getPlatformAdapter().getMsg().getString("Name." + name());  
  }
  
  public static ScreenComponent valueOf(IComponentType cType) {
    for (ScreenComponent screenComponent: ScreenComponent.values()) {
      if (screenComponent.name().equals(cType.name())) {
        return screenComponent;
      }
    }
    return null;
  }
}
