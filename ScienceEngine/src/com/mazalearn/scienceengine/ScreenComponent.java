package com.mazalearn.scienceengine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.app.screens.HelpTour.IHelpComponent;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.view.AnimateAction;

public enum ScreenComponent implements IComponentType, IHelpComponent {
  TopBar(XAlign.LEFT, 0, YAlign.TOP, 0, 800, 30, 0, Color.BLACK, true, false),
  BottomBar(XAlign.LEFT, 0, YAlign.BOTTOM, 0, 800, 20, 0, Color.BLACK, true, false),
  Title(XAlign.CENTER, 0, YAlign.TOP, -10, 0, 0, -1, Color.WHITE, true, false),
  Status(XAlign.CENTER, 0, YAlign.BOTTOM, 10, 0, 0, -1, Color.WHITE, true, false),
  User(XAlign.RIGHT, 0, YAlign.TOP, 0, 30, 29, -1, Color.WHITE, true, true),
  Back(XAlign.LEFT, 0, YAlign.TOP, 0, 70, 30, -1, Color.CLEAR, true, false),
  Help(XAlign.LEFT, 20, YAlign.TOP, -30, 40, 40, -1, Color.CLEAR, false, true),
  ViewControls(XAlign.LEFT, 81, YAlign.TOP, 0, 0, 0, -1, Color.CLEAR, true, false),
  ModelControls(XAlign.RIGHT, -20, YAlign.MIDDLE, 0, 0, 0, -1, Color.CLEAR, false, true),
  Logo(XAlign.RIGHT, 0, YAlign.BOTTOM, 0, 20, 20, -1, Color.CLEAR, true, false),
  Scoreboard(XAlign.RIGHT, -140, YAlign.TOP, 0, 40, 30, -1, Color.BLACK, true, true),
  Idea(XAlign.LEFT, 40, YAlign.TOP, -50, 50, 60, -1, Color.BLACK, false, false),
  NextButton(XAlign.RIGHT, -125, YAlign.TOP, -60, 0, 0, -1, Color.CLEAR, false, false), 
  ImageMessageBox(XAlign.LEFT, 10, YAlign.MIDDLE, 0, 0, 0, -1, Color.CLEAR, false, false), 
  Goal(XAlign.CENTER, 0, YAlign.TOP, -30, 550, 30, -1, Color.YELLOW, false, true),
  McqOption(XAlign.CENTER, 0, YAlign.MIDDLE, 100, 0, 0, -1, Color.YELLOW, false, false), 
  TimeTracker(XAlign.LEFT, 5, YAlign.BOTTOM, 2, 0, 0, -1, Color.CLEAR, false, false), 
  McqProgressInfo(XAlign.RIGHT, -40, YAlign.TOP, -32, 60, 40, -1, Color.YELLOW, false, false), 
  Hint(XAlign.RIGHT, -5, YAlign.TOP, -32, 115, 70, -1, Color.YELLOW, false, false), 
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
  private final int zIndex;

  public static final String CORE_GROUP = "CoreGroup";
  public static final String TUTOR_GROUP = "TutorGroup";
  public static final String ACTIVITY_GROUP = "ActivityGroup";
  public static final String HELP_TOUR = "HelpTour";
  public static final int PIXELS_PER_M = 8;
  
  static float X_SCALE = 1;
  static float Y_SCALE = 1;  
  private final static int CANONICAL_VIEWPORT_HEIGHT = 480;
  private final static int CANONICAL_VIEWPORT_WIDTH = 800;
  public static int VIEWPORT_HEIGHT = CANONICAL_VIEWPORT_HEIGHT;
  public static int VIEWPORT_WIDTH = CANONICAL_VIEWPORT_WIDTH;
  private final static int CANONICAL_FONT_SIZE = 14;
  private static int FontSize = CANONICAL_FONT_SIZE;
  // Initial entry is a flag
  private static int[] AVAILABLE_FONT_SIZES = {0, 8, 9, 10, 12, 14, 16, 20, 26};

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
      int width, int height, int zIndex, Color color, boolean inAllScreens, boolean helpTour) {
    this.xAlign = xAlign;
    this.xOffset = xOffset;
    this.yAlign = yAlign;
    this.yOffset = yOffset;
    this.canonicalWidth = width;
    this.canonicalHeight = height;
    this.zIndex = zIndex;
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
  
  public String getLocalizedName() {
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
    int fontSize = (int) Math.floor(Math.min(X_SCALE, Y_SCALE) * CANONICAL_FONT_SIZE);
    FontSize = selectFontSize(fontSize);
    for (ScreenComponent sc: values()) {
      sc.width = sc.canonicalWidth * X_SCALE;
      sc.height = sc.canonicalHeight * Y_SCALE;
      sc.x = sc.getX(sc.width);
      sc.y = sc.getY(sc.height);
    }
  }

  /**
   * @return closest available font size to <code>fontSize</code>
   * package protected for testing 
   */
  static int selectFontSize(int fontSize) {
    int index = AVAILABLE_FONT_SIZES.length - 1;
    for (int i = 1; i < AVAILABLE_FONT_SIZES.length; i++) {
      if (Math.abs(AVAILABLE_FONT_SIZES[i] - fontSize) < Math.abs(AVAILABLE_FONT_SIZES[index] - fontSize)) {
        index = i;
      }
    }
    return AVAILABLE_FONT_SIZES[index];
  }
  
  public static int getFontSize() {
    return FontSize;
  }

  // should be called only after setSize has been called
  public static String getFont(float relativeScaling) {
    int fontsize = selectFontSize(Math.round(getFontSize() * relativeScaling));
    Gdx.app.log(ScienceEngine.LOG, "Font chosen size: " + fontsize);
    return "font" + String.valueOf(fontsize);
  }

  @Override
  public String getComponentType() {
    return name();
  }

  public int getZIndex() {
    return zIndex;
  }

  @Override
  public void showHelp(Stage stage, boolean animate) {
    Actor actor = stage.getRoot().findActor(name());
    if (actor == null) return;
    if (animate) {
      actor.addAction(
          AnimateAction.animatePosition(actor.getX(), actor.getY(), actor.isVisible()));
    } else {
      actor.clearActions();
    }
  }
}
