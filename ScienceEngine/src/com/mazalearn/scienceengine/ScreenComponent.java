package com.mazalearn.scienceengine;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.core.model.IComponentType;

enum Align { LEFT(0), CENTER(400), RIGHT(800), 
             TOP(480), BOTTOM(0), MIDDLE(240);
  int base;
  int getValue(int offset) {
    return base + offset;
  }
  private Align(int base) { this.base = base; }
};

public enum ScreenComponent implements IComponentType {
  Title(Align.CENTER, 0, Align.TOP, -15, Color.WHITE, true, true),
  Status(Align.CENTER, 0, Align.BOTTOM, 10, Color.WHITE, true, true),
  User(Align.RIGHT, -70, Align.TOP, -15, Color.WHITE, true, true),
  Back(Align.LEFT, 2, Align.TOP, -30, Color.CLEAR, true, true), 
  ViewControls(Align.LEFT, 95, Align.TOP, -30, Color.CLEAR, true, true),
  ActivityViewControls(Align.LEFT, 95, Align.TOP, -60, Color.CLEAR, false, false),
  ModelControls(Align.RIGHT, -90, Align.MIDDLE, 0, Color.CLEAR, false, true),
  GoButtonUp(Align.LEFT, 10, Align.MIDDLE, 0, Color.CLEAR, false, true),
  GoButtonDown(Align.LEFT, 120, Align.TOP, -30, Color.CLEAR, false, false),
  NextButton(Align.MIDDLE, 170, Align.TOP, -130, Color.CLEAR, false, false),
  ;
  
  private int x, y;
  private Color color;
  private boolean inAllScreens;
  private boolean helpTour;
  private Align alignX, alignY;
  public static final int PIXELS_PER_M = 8;
  
  private static float X_SCALE, Y_SCALE;  
  private static int CANONICAL_VIEWPORT_HEIGHT = 480;
  private static int CANONICAL_VIEWPORT_WIDTH = 800;
  public static int VIEWPORT_HEIGHT = CANONICAL_VIEWPORT_HEIGHT;
  public static int VIEWPORT_WIDTH = CANONICAL_VIEWPORT_WIDTH;

  private ScreenComponent(Align alignX, int x, Align alignY, int y, Color color, boolean inAllScreens, boolean helpTour) {
    this.alignX = alignX;
    this.x = x;
    this.alignY = alignY;
    this.y = y;
    this.color = color;
    this.inAllScreens = inAllScreens;
    this.helpTour = helpTour;
  }
  
  public int getX() { return alignX.getValue(x); }
  
  public int getY() { return alignY.getValue(y); }
  
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
  
  public static float getScaledX(float x) {
    return x * X_SCALE;
  }

  public static float getScaledY(float y) {
    return y * Y_SCALE;
  }

  public static void setSize(int width, int height) {
    VIEWPORT_WIDTH = width;
    VIEWPORT_HEIGHT = height;
    Align.CENTER.base = VIEWPORT_WIDTH / 2;
    Align.MIDDLE.base = VIEWPORT_HEIGHT / 2;
    Align.RIGHT.base = VIEWPORT_WIDTH;
    Align.TOP.base = VIEWPORT_HEIGHT;
    X_SCALE = VIEWPORT_WIDTH / (float) CANONICAL_VIEWPORT_WIDTH;
    Y_SCALE = VIEWPORT_HEIGHT / (float) CANONICAL_VIEWPORT_HEIGHT;
  }
}
