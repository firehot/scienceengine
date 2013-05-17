package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;

public class DrawingActor extends Actor {
  private static final int LINE_WIDTH = 5;
  public static final int FACE_HEIGHT = 64;
  public static final int FACE_WIDTH = 64;
  private static int SCALE = 4;
  public static int SCALED_FACE_WIDTH = SCALE * FACE_WIDTH;
  public static int SCALED_FACE_HEIGHT = SCALE * FACE_HEIGHT;
  static {
    SCALE = (ScreenComponent.VIEWPORT_WIDTH < 800) ? 2 : 4;      
    SCALED_FACE_WIDTH = SCALE * FACE_WIDTH;
    SCALED_FACE_HEIGHT = SCALE * FACE_HEIGHT;
  }
  private Vector2 pos = new Vector2(), prevPos = new Vector2();
  private ShapeRenderer shapeRenderer;
  private Texture faceTexture;
  private boolean hasChangedSinceSnapshot = false;
  private Pixmap snapshot;
  private Face face;
  private List<List<Vector2>> pointSequences = new ArrayList<List<Vector2>>();

  public static class Face extends Group {
    private Label userCurrentLabel;
    
    private Face(Texture faceTexture, Skin skin) {
      super();
      userCurrentLabel = new Label(ScienceEngine.getUserName(), skin);
      userCurrentLabel.setPosition(0, FACE_HEIGHT);
      
      Image face = new Image(faceTexture);
      this.setSize(FACE_WIDTH, FACE_HEIGHT);
      face.setPosition(0, 0);
      
      this.addActor(userCurrentLabel);
      this.addActor(face);
    }
  }
  
  public void addPointSequence() {
    // Not useful to have a sequence of 0 or 1 pointSequences
    if (pointSequences.size() > 1 && pointSequences.get(pointSequences.size() - 1).size() <= 1) {
      pointSequences.get(pointSequences.size() - 1).clear();
      return;
    }
    pointSequences.add(new ArrayList<Vector2>());
  }
  
  public void addPoint(float x, float y) {
    pointSequences.get(pointSequences.size() - 1).add(new Vector2(x, y));
  }

  public DrawingActor(Skin skin) {
    this.shapeRenderer = new ShapeRenderer();
    // snapshot will contain image of face + 2 eyes
    Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    this.snapshot = profile.getUserPixmap();
    if (snapshot == null) {
      this.snapshot = new Pixmap(FACE_WIDTH, FACE_HEIGHT, Format.RGBA8888);
    }
    hasChangedSinceSnapshot = false;
    this.faceTexture = new Texture(snapshot);
    this.face = new Face(faceTexture, skin);
    this.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        addPointSequence();
        return true;
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (pointSequences.size() >= 1) {
          hasChangedSinceSnapshot = true;
        }
      }

      @Override
      public void touchDragged(InputEvent event, float localX, float localY, int pointer) {
        if (DrawingActor.this.hit(localX, localY, true) == null) return;
        // Use coordinates at origin
        pos.set(localX, localY).mul(1f / ScreenComponent.PIXELS_PER_M);
        addPoint(pos.x, pos.y);
      }
    });
  }
  
  public void takeSnapshot() {
    localToStageCoordinates(pos.set(0, 0));
    Pixmap screenShot = ScreenUtils.getScreenshot(
        pos.x, pos.y, SCALED_FACE_WIDTH, SCALED_FACE_HEIGHT, 
        FACE_WIDTH, FACE_HEIGHT, getStage(), true);
    Blending b = Pixmap.getBlending();
    Pixmap.setBlending(Blending.None);
    snapshot.drawPixmap(screenShot, 0, 0);
    faceTexture.draw(snapshot, 0, 0);
    Pixmap.setBlending(b);
    screenShot.dispose();
    hasChangedSinceSnapshot = false;
  }
  
  public boolean hasChangedSinceSnapshot() {
    return hasChangedSinceSnapshot;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    batch.end();
    drawFaceAndEyesAreas(pointSequences);
    if (hasChangedSinceSnapshot) {
      takeSnapshot();
    }
    batch.begin();
  }

  private void drawFaceAndEyesAreas(List<List<Vector2>> pointSequences) {
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    shapeRenderer.identity();
    localToStageCoordinates(pos.set(0, 0));
    shapeRenderer.translate(pos.x, pos.y, 0);
    shapeRenderer.setColor(Color.BLACK);
    shapeRenderer.begin(ShapeType.FilledRectangle);
    shapeRenderer.filledRect(0,  0, SCALED_FACE_WIDTH, SCALED_FACE_HEIGHT);
    shapeRenderer.end();
    
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(Color.YELLOW);
    
    // Draw face area
    shapeRenderer.rect(0, 0, SCALED_FACE_WIDTH, SCALED_FACE_HEIGHT);
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

  public Face getFace() {
    return face;
  }
  
  public Pixmap getPixmap() {
    return snapshot;
  }
}