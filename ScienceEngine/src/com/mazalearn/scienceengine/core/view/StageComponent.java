package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum StageComponent implements IComponentType {
  Title(250, -10, Color.YELLOW),
  Status(10, 20, Color.RED),
  ViewControls(740, -30, Color.BLACK), 
  ControlPanel(709, 262, Color.BLACK);
  
  private int x;
  private int y;
  private Color color;

  private StageComponent(int x, int y, Color color) {
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
  
  public static StageComponent valueOf(IComponentType cType) {
    for (StageComponent stageComponent: StageComponent.values()) {
      if (stageComponent.name().equals(cType.name())) {
        return stageComponent;
      }
    }
    return null;
  }
}
