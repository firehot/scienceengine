package com.mazalearn.scienceengine;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.core.model.IComponentType;

enum Align { LEFT(0, true), CENTER(400, true), RIGHT(800, true), 
             TOP(480, false), BOTTOM(0, false), MIDDLE(240, false);
  int base;
  private boolean xDimension;
  float getValue(int offset) {
    return base + (xDimension ? ScreenComponent.X_SCALE : ScreenComponent.Y_SCALE) * offset;
  }
  
  private Align(int base, boolean xDimension) { 
    this.base = base;
    this.xDimension = xDimension;
  }
};

public enum ScreenComponent implements IComponentType {
  Title(Align.CENTER, 0, Align.TOP, -5, Color.WHITE, true, true),
  Status(Align.CENTER, 0, Align.BOTTOM, 5, Color.WHITE, true, true),
  User(Align.RIGHT, -70, Align.TOP, -2, Color.WHITE, true, true),
  Back(Align.LEFT, 0, Align.TOP, 0, Color.CLEAR, true, true), 
  ViewControls(Align.LEFT, 81, Align.TOP, 0, Color.CLEAR, true, true),
  ModelControls(Align.RIGHT, -20, Align.MIDDLE, 0, Color.CLEAR, false, true),
  GoButtonUp(Align.LEFT, 10, Align.MIDDLE, 0, Color.CLEAR, false, true),
  GoButtonDown(Align.LEFT, 120, Align.TOP, 0, Color.CLEAR, false, false),
  NextButton(Align.CENTER, 108, Align.TOP, -50, Color.CLEAR, false, false),
  ;
  
  private int xOffset, yOffset;
  private Color color;
  private boolean inAllScreens;
  private boolean helpTour;
  private Align alignX, alignY;
  public static final int PIXELS_PER_M = 8;
  
  static float X_SCALE;
  static float Y_SCALE;  
  private static int CANONICAL_VIEWPORT_HEIGHT = 480;
  private static int CANONICAL_VIEWPORT_WIDTH = 800;
  public static int VIEWPORT_HEIGHT = CANONICAL_VIEWPORT_HEIGHT;
  public static int VIEWPORT_WIDTH = CANONICAL_VIEWPORT_WIDTH;

  /**
   * Constructor 
   * Specifies how closest point of component is placed on the screen.
   * e.g. alignX being LEFT means LEFT edge of component is positioned
   * e.g. alignY being MIDDLE means MIDDLE of component is positioned
   * @param alignX - part of screen wrt which X offset is specified - LEFT, CENTER, RIGHT
   * @param xOffset - xOffset offset
   * @param alignY - part of screen wrt which Y offset is specified - LEFT, CENTER, RIGHT
   * @param yOffset - yOffset offset
   * @param color - color of component
   * @param inAllScreens - whether this is present in all screens
   * @param helpTour - whether this component should be part of help tour
   */
  private ScreenComponent(Align alignX, int xOffset, Align alignY, int yOffset, Color color, boolean inAllScreens, boolean helpTour) {
    this.alignX = alignX;
    this.xOffset = xOffset;
    this.alignY = alignY;
    this.yOffset = yOffset;
    this.color = color;
    this.inAllScreens = inAllScreens;
    this.helpTour = helpTour;
  }
  
  public float getX(float width) {
    switch (alignX ) {
    case LEFT: return alignX.getValue(xOffset);
    case CENTER: return alignX.getValue(xOffset) - width / 2;
    case RIGHT: return alignX.getValue(xOffset) - width;
    }
    return 0;
  }
  
  public float getY(float height) {
    switch (alignY ) {
    case BOTTOM: return alignY.getValue(yOffset);
    case MIDDLE: return alignY.getValue(yOffset) - height / 2;
    case TOP: return alignY.getValue(yOffset) - height;
    }
    return 0;
  }
  
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
