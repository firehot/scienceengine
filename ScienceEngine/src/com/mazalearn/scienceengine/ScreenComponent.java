package com.mazalearn.scienceengine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ScreenComponent implements IComponentType {
  Background(Align.LEFT, 0, Align.BOTTOM, 0, 800, 480, Color.BLACK, false, false),
  Dashboard(Align.CENTER, 0, Align.TOP, -3, 0, 0, Color.BLACK, false, false),
  ShoppingCart(Align.LEFT, 40, Align.TOP, -50, 50, 50, Color.BLACK, false, false),
  Prober(Align.LEFT, 0, Align.BOTTOM, 0, 800, 450, Color.CLEAR, false, false),
  Title(Align.CENTER, 0, Align.TOP, -10, 0, 0, Color.WHITE, true, false),
  Status(Align.CENTER, 0, Align.BOTTOM, 10, 0, 0, Color.WHITE, true, false),
  User(Align.RIGHT, -70, Align.TOP, -2, 20, 30, Color.WHITE, true, false),
  Back(Align.LEFT, 0, Align.TOP, 0, 70, 30, Color.CLEAR, true, false), 
  ViewControls(Align.LEFT, 81, Align.TOP, 0, 0, 0, Color.CLEAR, true, true),
  ModelControls(Align.RIGHT, -20, Align.MIDDLE, 0, 0, 0, Color.CLEAR, false, true),
  GoButtonUp(Align.LEFT, 10, Align.MIDDLE, 0, 60, 60, Color.CLEAR, false, true),
  GoButtonDown(Align.LEFT, 120, Align.TOP, 0, 30, 30, Color.CLEAR, false, false),
  NextButton(Align.CENTER, 108, Align.TOP, -35, 0, 0, Color.CLEAR, false, false),
  ;
  
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

private int xOffset, yOffset;
  private Color color;
  private boolean inAllScreens;
  private boolean helpTour;
  private Align alignX, alignY;
  private int width;
  private int height;
  public static final int PIXELS_PER_M = 8;
  
  static float X_SCALE = 1;
  static float Y_SCALE = 1;  
  private final static int CANONICAL_VIEWPORT_HEIGHT = 480;
  private final static int CANONICAL_VIEWPORT_WIDTH = 800;
  public static int VIEWPORT_HEIGHT = CANONICAL_VIEWPORT_HEIGHT;
  public static int VIEWPORT_WIDTH = CANONICAL_VIEWPORT_WIDTH;
  private final static int CANONICAL_FONT_SIZE = 16;
  private static int FontSize = CANONICAL_FONT_SIZE;
  // Initial entry is a flag
  private static int[] AVAILABLE_FONT_SIZES = {0, 12, 15, 16, 20, 26};

  /**
   * Constructor 
   * Specifies how closest point of component is placed on the screen.
   * e.g. alignX being LEFT means LEFT edge of component is positioned
   * e.g. alignY being MIDDLE means MIDDLE of component is positioned
   * @param alignX - part of screen wrt which X offset is specified - LEFT, CENTER, RIGHT
   * @param xOffset - xOffset offset
   * @param alignY - part of screen wrt which Y offset is specified - LEFT, CENTER, RIGHT
   * @param yOffset - yOffset offset
   * @param width - canonical width - 0 indicates self-adjusting
   * @param height - canonical height - 0 indicates self-adjusting
   * @param color - color of component
   * @param inAllScreens - whether this is present in all screens
   * @param helpTour - whether this component should be part of help tour
   */
  private ScreenComponent(Align alignX, int xOffset, Align alignY, int yOffset, 
      int width, int height, Color color, boolean inAllScreens, boolean helpTour) {
    this.alignX = alignX;
    this.xOffset = xOffset;
    this.alignY = alignY;
    this.yOffset = yOffset;
    this.width = width;
    this.height = height;
    this.color = color;
    this.inAllScreens = inAllScreens;
    this.helpTour = helpTour;
  }
  
  /**
   * @return scaled width
   */
  public float getWidth() {
    return width * X_SCALE;
  }
  
  /**
   * @return scaled height
   */
  public float getHeight() {
    return height * Y_SCALE;
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
    int fontSize = Math.round(Math.min(X_SCALE, Y_SCALE) * CANONICAL_FONT_SIZE);
    FontSize = AVAILABLE_FONT_SIZES[AVAILABLE_FONT_SIZES.length - 1];
    for (int i = 1; i < AVAILABLE_FONT_SIZES.length; i++) {
      if (AVAILABLE_FONT_SIZES[i-1] < fontSize && fontSize <= AVAILABLE_FONT_SIZES[i]) {
        FontSize = AVAILABLE_FONT_SIZES[i];
        break;
      }
    }
  }
  
  // should be called only after setSize has been called
  public static String getFont() {
    Gdx.app.log(ScienceEngine.LOG, "Font chosen size: " + FontSize);
    return "font" + String.valueOf(FontSize);
  }
}
