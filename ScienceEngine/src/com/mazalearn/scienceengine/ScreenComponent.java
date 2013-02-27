package com.mazalearn.scienceengine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.app.screens.HelpTour.IHelpComponent;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ScreenComponent implements IComponentType, IHelpComponent {
  Background(XAlign.LEFT, 0, YAlign.BOTTOM, 0, 800, 480, Color.BLACK, false, false),
  Dashboard(XAlign.CENTER, 0, YAlign.TOP, -3, 0, 0, Color.BLACK, false, false),
  ShoppingCart(XAlign.LEFT, 40, YAlign.TOP, -50, 50, 50, Color.BLACK, false, false),
  Prober(XAlign.LEFT, 0, YAlign.BOTTOM, 0, 800, 450, Color.CLEAR, false, false),
  Title(XAlign.CENTER, 0, YAlign.TOP, -10, 0, 0, Color.WHITE, true, false),
  Status(XAlign.CENTER, 0, YAlign.BOTTOM, 10, 0, 0, Color.WHITE, true, false),
  User(XAlign.RIGHT, -20, YAlign.TOP, -2, 20, 30, Color.WHITE, true, false),
  Back(XAlign.LEFT, 0, YAlign.TOP, 0, 70, 30, Color.CLEAR, true, false), 
  ViewControls(XAlign.LEFT, 81, YAlign.TOP, 0, 0, 0, Color.CLEAR, true, true),
  ModelControls(XAlign.RIGHT, -20, YAlign.MIDDLE, 0, 0, 0, Color.CLEAR, false, true),
  GoButtonUp(XAlign.LEFT, 10, YAlign.MIDDLE, 0, 60, 60, Color.CLEAR, false, true),
  GoButtonDown(XAlign.CENTER, 0, YAlign.TOP, -30, 0, 0, Color.CLEAR, false, false),
  NextButton(XAlign.CENTER, 108, YAlign.TOP, -50, 0, 0, Color.CLEAR, false, false), 
  Goal(XAlign.CENTER, 0, YAlign.TOP, -30, 800, 0, Color.YELLOW, false, false),
  McqOption(XAlign.CENTER, 0, YAlign.MIDDLE, 100, 0, 0, Color.YELLOW, false, false),
  ;
  
  enum XAlign { LEFT(0), CENTER(800 / 2), RIGHT(800); 
    int base;
    
    float getValue(int offset) {
      return base + ScreenComponent.X_SCALE * offset;
    }
    
    private XAlign(int base) { 
      this.base = base;
    }
  };
  
  enum YAlign { BOTTOM(0), MIDDLE(480 / 2), TOP(480); 
    int base;
    
    float getValue(int offset) {
      return base + ScreenComponent.Y_SCALE * offset;
    }
    
    private YAlign(int base) { 
      this.base = base;
    }
  };

  private final int xOffset, yOffset;
  private final Color color;
  private final boolean inAllScreens;
  private final boolean helpTour;
  private final XAlign xAlign;
  private final YAlign yAlign;
  private final int canonicalWidth;
  private final int canonicalHeight;
  private float x, y, width, height;
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
   * e.g. xAlign being LEFT means LEFT edge of component is positioned
   * e.g. yAlign being MIDDLE means MIDDLE of component is positioned
   * @param xAlign - part of screen wrt which X offset is specified
   * @param xOffset - xOffset offset
   * @param yAlign - part of screen wrt which Y offset is specified
   * @param yOffset - yOffset offset
   * @param canonicalWidth - canonical canonicalWidth - 0 indicates self-adjusting
   * @param canonicalHeight - canonical canonicalHeight - 0 indicates self-adjusting
   * @param color - color of component
   * @param inAllScreens - whether this is present in all screens
   * @param helpTour - whether this component should be part of help tour
   */
  private ScreenComponent(XAlign xAlign, int xOffset, YAlign yAlign, int yOffset, 
      int width, int height, Color color, boolean inAllScreens, boolean helpTour) {
    this.xAlign = xAlign;
    this.xOffset = xOffset;
    this.yAlign = yAlign;
    this.yOffset = yOffset;
    this.canonicalWidth = width;
    this.canonicalHeight = height;
    this.color = color;
    this.inAllScreens = inAllScreens;
    this.helpTour = helpTour;
  }
  
  /**
   * @return scaled canonicalWidth
   */
  public float getWidth() {
    return width;
  }
  
  /**
   * @return scaled canonicalHeight
   */
  public float getHeight() {
    return height;
  }
  
  public float getX() { return x; }
  public float getY() { return y; }
  
  public float getX(float width) {
    switch (xAlign ) {
    case LEFT: return xAlign.getValue(xOffset);
    case CENTER: return xAlign.getValue(xOffset) - width / 2;
    case RIGHT: return xAlign.getValue(xOffset) - width;
    }
    return 0;
  }
  
  public float getY(float height) {
    switch (yAlign ) {
    case BOTTOM: return yAlign.getValue(yOffset);
    case MIDDLE: return yAlign.getValue(yOffset) - height / 2;
    case TOP: return yAlign.getValue(yOffset) - height;
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
  
  public static void scaleSize(Actor actor, float cWidth, float cHeight) {
    actor.setSize(getScaledX(cWidth), getScaledY(cHeight));
  }

  public static void scalePosition(Actor actor, float cX, float cY) {
    actor.setPosition(getScaledX(cX), getScaledY(cY));
  }

  public static void scalePositionAndSize(Actor actor, float cX, float cY, float cWidth, float cHeight) {
    scaleSize(actor, cWidth, cHeight);
    scalePosition(actor, cX, cY);
  }

  public static void setSize(int width, int height) {
    VIEWPORT_WIDTH = width;
    VIEWPORT_HEIGHT = height;
    XAlign.CENTER.base = VIEWPORT_WIDTH / 2;
    YAlign.MIDDLE.base = VIEWPORT_HEIGHT / 2;
    XAlign.RIGHT.base = VIEWPORT_WIDTH;
    YAlign.TOP.base = VIEWPORT_HEIGHT;
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
    for (ScreenComponent sc: values()) {
      sc.width = sc.canonicalWidth * X_SCALE;
      sc.height = sc.canonicalHeight * Y_SCALE;
      sc.x = sc.getX(sc.width);
      sc.y = sc.getY(sc.height);
    }
  }
  
  // should be called only after setSize has been called
  public static String getFont() {
    Gdx.app.log(ScienceEngine.LOG, "Font chosen size: " + FontSize);
    return "font" + String.valueOf(FontSize);
  }

  @Override
  public String getName() {
    return name();
  }
}
