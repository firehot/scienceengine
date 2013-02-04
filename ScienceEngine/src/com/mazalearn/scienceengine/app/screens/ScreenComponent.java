package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ScreenComponent implements IComponentType {
  Title(400, 465, Color.WHITE, true),
  Status(400, 10, Color.WHITE, true),
  User(730, 465, Color.WHITE, true),
  BackButton(2, -30, Color.CLEAR, true), 
  ViewControls(95, -30, Color.CLEAR, true),
  ActivityViewControls(95, -60, Color.CLEAR, false),
  ModelControls(710, 232, Color.CLEAR, false),
  GoButtonUp(10, 220, Color.CLEAR, false),
  GoButtonDown(120, -30, Color.CLEAR, false);
  
  private int x;
  private int y;
  private Color color;
  private boolean inAllScreens;

  private ScreenComponent(int x, int y, Color color, boolean inAllScreens) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.inAllScreens = inAllScreens;
  }
  
  public int getX() { return x < 0 ? AbstractScreen.VIEWPORT_WIDTH + x : x; }
  
  public int getY() { return y < 0 ? AbstractScreen.VIEWPORT_HEIGHT + y : y; }
  
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

  public boolean isInAllScreens() {
    return inAllScreens;
  }
}
