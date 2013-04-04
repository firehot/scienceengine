package com.mazalearn.scienceengine.domains.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Drawing;

public class DrawingActor extends Science2DActor {
  private static final int LINE_WIDTH = 4;
  private static final float SCALE = 4f;
  private static final int WHEEL_DIA = 23;
  private static final int COACH_HEIGHT = 64;
  private static final int COACH_WIDTH = 105;
  private static final int WHEEL_OFFSET = 20;
  private static float DRAWING_WHEEL_DIA = SCALE * (WHEEL_DIA + 2);
  private static float DRAWING_WHEEL_OFFSET = SCALE * (WHEEL_OFFSET - 1);
  private static float DRAWING_COACH_WIDTH = SCALE * COACH_WIDTH;
  private static float DRAWING_COACH_HEIGHT = SCALE * COACH_HEIGHT;
  private final Drawing drawing;
  private Vector2 pos = new Vector2(), prevPos = new Vector2();
  private ShapeRenderer shapeRenderer;
  private BitmapFont font;
  private Texture coachTexture;
  private boolean hasChangedSinceSnapshot = false;
  private Pixmap snapshot;
  private Coach coach;
  private String viewSpec;
  
  static {
    // Scale screen sizes based on screen dimensions
    DRAWING_WHEEL_DIA = ScreenComponent.getScaledX(SCALE * (WHEEL_DIA + 2));
    DRAWING_WHEEL_OFFSET = ScreenComponent.getScaledX(SCALE * (WHEEL_OFFSET - 1));
    DRAWING_COACH_WIDTH = ScreenComponent.getScaledX(SCALE * COACH_WIDTH);
    DRAWING_COACH_HEIGHT = ScreenComponent.getScaledY(SCALE * COACH_HEIGHT);
  }
  public static class Coach extends Group {
    private Label userCurrentLabel;
    private Color lightColor = Color.YELLOW;
    private float lightIntensity = 0;
    
    private Coach(Texture coachTexture, Skin skin) {
      super();
      userCurrentLabel = new Label(ScienceEngine.getUserName(), skin);
      userCurrentLabel.setPosition(0, COACH_HEIGHT);
      
      Image coachBody = new Image(new TextureRegion(coachTexture, 0, 0, COACH_WIDTH, COACH_HEIGHT));
      this.setSize(COACH_WIDTH, COACH_HEIGHT);
      coachBody.setPosition(0, 0);
      
      Image wheel1 = new Image(new TextureRegion(coachTexture, COACH_WIDTH, 0, WHEEL_DIA, WHEEL_DIA));
      wheel1.setPosition(WHEEL_OFFSET, 0);
      wheel1.setOrigin(WHEEL_DIA/2, WHEEL_DIA/2);
      wheel1.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      
      Image wheel2 = new Image(new TextureRegion(coachTexture, COACH_WIDTH, WHEEL_DIA, WHEEL_DIA, WHEEL_DIA));
      wheel2.setPosition(COACH_WIDTH - WHEEL_OFFSET - WHEEL_DIA, 0);
      wheel2.setOrigin(WHEEL_DIA/2, WHEEL_DIA/2);
      wheel2.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      
      this.addActor(userCurrentLabel);
      this.addActor(coachBody);
      this.addActor(wheel1);
      this.addActor(wheel2);
    }
    
    public void setLight(float intensity, Color color) {
      userCurrentLabel.setText(userCurrentLabel.getText() + "  " + String.valueOf(intensity));
      this.lightColor = color;
      this.lightIntensity = intensity;
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
      super.draw(batch, parentAlpha);
      if (lightIntensity != 0) {
        float intensity = lightIntensity * MathUtils.sinDeg(getParent().getX());
        LightbulbActor.drawLight(batch, intensity, 32 * intensity, lightColor, 
            getX() + COACH_WIDTH / 2, getY() + COACH_HEIGHT / 2);
      }      
    }
  }
  
  public DrawingActor(Science2DBody body, TextureRegion textureRegion, 
      String name, BitmapFont font, Skin skin) {
    super(body, textureRegion);
    this.drawing = (Drawing) body;
    this.font = font;
    this.viewSpec = name;
    this.shapeRenderer = new ShapeRenderer();
    // snapshot will contain image of coach + 2 wheels
    Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    this.snapshot = profile.getCoachPixmap();
    if (snapshot == null) {
      this.snapshot = new Pixmap(COACH_WIDTH + WHEEL_DIA, COACH_HEIGHT, Format.RGBA8888);
    }
    hasChangedSinceSnapshot = false;
    this.coachTexture = new Texture(snapshot);
    this.coach = new Coach(coachTexture, skin);
    this.removeListener(getListeners().get(0));
    this.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        drawing.addPointSequence();
        drawing.setPositionAndAngle(drawing.getPosition(), 0);
        return true;
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (drawing.getPointSequences().size() >= 1) {
          hasChangedSinceSnapshot = true;
        }
      }

      @Override
      public void touchDragged(InputEvent event, float localX, float localY, int pointer) {
        if (DrawingActor.this.hit(localX, localY, true) == null) return;
        // Use coordinates at origin
        pos.set(localX, localY).mul(1f / ScreenComponent.PIXELS_PER_M);
        drawing.addPoint(pos.x, pos.y);
      }
    });
  }
  
  public void takeSnapshot() {
    Pixmap screenShot = ScreenUtils.getScreenshot(
        getX(), getY(), DRAWING_COACH_WIDTH, DRAWING_COACH_HEIGHT, 
        COACH_WIDTH, COACH_HEIGHT, getStage(), true);
    Blending b = Pixmap.getBlending();
    Pixmap.setBlending(Blending.None);
    snapshot.drawPixmap(screenShot, 0, 0);
    
    // Blank out the wheels in the coachBody
    snapshot.setColor(0);
    snapshot.fillRectangle(WHEEL_OFFSET - 1, COACH_HEIGHT - WHEEL_DIA - 3, 
        WHEEL_DIA + 3, WHEEL_DIA + 3);
    snapshot.fillRectangle(COACH_WIDTH - WHEEL_OFFSET - WHEEL_DIA - 1, 
        COACH_HEIGHT - WHEEL_DIA - 3, WHEEL_DIA + 3, WHEEL_DIA + 3);
    // Draw wheels on side
    snapshot.drawPixmap(screenShot, 
        WHEEL_OFFSET, COACH_HEIGHT - WHEEL_DIA - 1, WHEEL_DIA, WHEEL_DIA, 
        COACH_WIDTH, 0, WHEEL_DIA, WHEEL_DIA);
    snapshot.drawPixmap(screenShot,
        COACH_WIDTH - WHEEL_OFFSET - WHEEL_DIA, COACH_HEIGHT - WHEEL_DIA - 1, 
        WHEEL_DIA, WHEEL_DIA, 
        COACH_WIDTH, WHEEL_DIA, WHEEL_DIA, WHEEL_DIA);

    // Workaround for GWT WebGL - TexSubImage2D gives an error, using TexImage2D instead.
    // But that also does not seem to work.
    if (ScienceEngine.getPlatformAdapter().getPlatform() == IPlatformAdapter.Platform.GWT) {
      PixmapTextureData pd = (PixmapTextureData) coachTexture.getTextureData();
      Pixmap p = pd.consumePixmap();
      p.drawPixmap(snapshot, 0, 0);
      coachTexture.load(pd);
    } else {   
      coachTexture.draw(snapshot, 0, 0);
    }
    Pixmap.setBlending(b);
    screenShot.dispose();
    hasChangedSinceSnapshot = false;
  }
  
  public boolean hasChangedSinceSnapshot() {
    return hasChangedSinceSnapshot;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    List<List<Vector2>> pointSequences = drawing.getPointSequences();
    if (pointSequences.size() < 1) {
      font.draw(batch, viewSpec, getX() + 20, getY() + 20);
    }
    batch.end();
    drawCoachAndWheelAreas(pointSequences);
    batch.begin();
  }

  private void drawCoachAndWheelAreas(List<List<Vector2>> pointSequences) {
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    shapeRenderer.identity();
    shapeRenderer.translate(getX(), getY(), 0);
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(Color.YELLOW);
    
    // Draw wheel areas
    shapeRenderer.rect(DRAWING_WHEEL_OFFSET, 0, DRAWING_WHEEL_DIA, DRAWING_WHEEL_DIA);
    shapeRenderer.rect(DRAWING_COACH_WIDTH - DRAWING_WHEEL_OFFSET - DRAWING_WHEEL_DIA, 0, 
        DRAWING_WHEEL_DIA, DRAWING_WHEEL_DIA);
    
    // Draw coach area
    shapeRenderer.rect(0, 0, DRAWING_COACH_WIDTH, DRAWING_COACH_HEIGHT);
    shapeRenderer.end();

    // Draw user's drawing
    shapeRenderer.setColor(Color.WHITE);
    shapeRenderer.begin(ShapeType.FilledRectangle);
    for (List<Vector2> pointSequence: pointSequences) {
      if (pointSequence.size() < 1) continue;
      prevPos.set(pointSequence.get(0));
      for (int i = 1; i < pointSequence.size(); i++) {
        pos.set(pointSequence.get(i)).sub(prevPos);
        shapeRenderer.translate(prevPos.x * ScreenComponent.PIXELS_PER_M, 
            prevPos.y * ScreenComponent.PIXELS_PER_M, 0);
        shapeRenderer.rotate(0, 0, 1, pos.angle());
        shapeRenderer.filledRect(0, 0, pos.len() * ScreenComponent.PIXELS_PER_M, LINE_WIDTH);
        shapeRenderer.rotate(0, 0, 1, -pos.angle());
        shapeRenderer.translate(-prevPos.x * ScreenComponent.PIXELS_PER_M, 
            -prevPos.y * ScreenComponent.PIXELS_PER_M, 0);
        prevPos.set(pointSequence.get(i));
      }
    }
    shapeRenderer.end();
  }

  public Coach getCoach() {
    return coach;
  }
  
  public Pixmap getPixmap() {
    return snapshot;
  }
}