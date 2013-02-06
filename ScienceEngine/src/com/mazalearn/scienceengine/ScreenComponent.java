package com.mazalearn.scienceengine;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ScreenComponent implements IComponentType {
  Title(400, 465, Color.WHITE, true, true),
  Status(400, 10, Color.WHITE, true, true),
  User(730, 465, Color.WHITE, true, true),
  Back(2, -30, Color.CLEAR, true, true), 
  ViewControls(95, -30, Color.CLEAR, true, true),
  ActivityViewControls(95, -60, Color.CLEAR, false, false),
  ModelControls(710, 232, Color.CLEAR, false, true),
  GoButtonUp(10, 220, Color.CLEAR, false, true),
  GoButtonDown(120, -30, Color.CLEAR, false, false);
  
  private int x;
  private int y;
  private Color color;
  private boolean inAllScreens;
  private boolean helpTour;
  public static final int VIEWPORT_HEIGHT = 480;
  public static final int VIEWPORT_WIDTH = 800;

  private ScreenComponent(int x, int y, Color color, boolean inAllScreens, boolean helpTour) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.inAllScreens = inAllScreens;
    this.helpTour = helpTour;
  }
  
  public int getX() { return x < 0 ? ScreenComponent.VIEWPORT_WIDTH + x : x; }
  
  public int getY() { return y < 0 ? ScreenComponent.VIEWPORT_HEIGHT + y : y; }
  
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
  
  public boolean showInHelpTour() {
    return helpTour;
  }
}
