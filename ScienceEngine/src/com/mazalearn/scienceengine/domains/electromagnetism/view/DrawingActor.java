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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Drawing;

public class DrawingActor extends Science2DActor {
  private static final int LINE_WIDTH = 4;
  private static final float SCALE = 4f;
  private static final int WHEEL_DIA = 23;
  private static final float DRAWING_WHEEL_DIA = SCALE * (WHEEL_DIA + 2);
  private static final int COACH_HEIGHT = 64;
  private static final int COACH_WIDTH = 105;
  private static final int WHEEL_OFFSET = 20;
  private static final float DRAWING_WHEEL_OFFSET = SCALE * (WHEEL_OFFSET - 1);
  private static final float DRAWING_COACH_WIDTH = SCALE * COACH_WIDTH;
  private static final float DRAWING_COACH_HEIGHT = SCALE * COACH_HEIGHT;
  private final Drawing drawing;
  private Vector2 pos = new Vector2(), prevPos = new Vector2();
  private ShapeRenderer shapeRenderer;
  private BitmapFont font;
  private Texture coachTexture;
  private boolean hasChangedSinceSnapshot = true; // Force an initial snapshot
  private Pixmap snapshot;
  private Group coach;
  private Image composite;
    
  public DrawingActor(Science2DBody body, TextureRegion textureRegion, BitmapFont font) {
    super(body, textureRegion);
    this.drawing = (Drawing) body;
    this.font = font;
    this.shapeRenderer = new ShapeRenderer();
    // snapshot will contain image of coach + 2 wheels
    this.snapshot = new Pixmap(COACH_WIDTH + WHEEL_DIA, COACH_HEIGHT, Format.RGBA8888);
    this.coachTexture = new Texture(snapshot);
    
    this.coach = new Group();
    Image coachBody = new Image(new TextureRegion(coachTexture, 0, 0, COACH_WIDTH, COACH_HEIGHT));
    coachBody.setPosition(0, 0);
    
    Image wheel1 = new Image(new TextureRegion(coachTexture, COACH_WIDTH, 0, WHEEL_DIA, WHEEL_DIA));
    wheel1.setPosition(WHEEL_OFFSET, 0);
    wheel1.setOrigin(WHEEL_DIA/2, WHEEL_DIA/2);
    wheel1.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
    
    Image wheel2 = new Image(new TextureRegion(coachTexture, COACH_WIDTH, WHEEL_DIA, WHEEL_DIA, WHEEL_DIA));
    wheel2.setPosition(COACH_WIDTH - WHEEL_OFFSET - WHEEL_DIA, 0);
    wheel2.setOrigin(WHEEL_DIA/2, WHEEL_DIA/2);
    wheel2.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
    
    this.composite = new Image(new TextureRegion(coachTexture));
    composite.setPosition(-COACH_WIDTH - 30, 0);
    
    coach.setSize(COACH_WIDTH, COACH_HEIGHT);
    coach.addActor(coachBody);
    coach.addActor(wheel1);
    coach.addActor(wheel2);
    coach.addActor(composite);
    
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
        pos.set(localX, localY).mul(1f / ScienceEngine.PIXELS_PER_M);
        drawing.addPoint(pos.x, pos.y);
      }
    });
  }
  
  public void takeSnapshot() {
    Pixmap screenShot = ScreenUtils.getScreenshot(
        getX(), getY(), DRAWING_COACH_WIDTH, DRAWING_COACH_HEIGHT, 
        SCALE, getStage(), true, false);
    Blending b = Pixmap.getBlending();
    Pixmap.setBlending(Blending.None);
    snapshot.drawPixmap(screenShot, 0, 0);
//        0, 0, COACH_WIDTH, COACH_HEIGHT, 
//        0, 0, COACH_WIDTH, COACH_HEIGHT);
    
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
    coachTexture.draw(snapshot, 0, 0);
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
      font.draw(batch, drawing.getExtra(), getX() + 20, getY() + 20);
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
    shapeRenderer.setColor(Color.WHITE);
    
    // Draw wheel areas
    shapeRenderer.rect(DRAWING_WHEEL_OFFSET, 0, DRAWING_WHEEL_DIA, DRAWING_WHEEL_DIA);
    shapeRenderer.rect(DRAWING_COACH_WIDTH - DRAWING_WHEEL_OFFSET - DRAWING_WHEEL_DIA, 0, 
        DRAWING_WHEEL_DIA, DRAWING_WHEEL_DIA);
    
    // Draw coach area
    shapeRenderer.rect(0, 0, DRAWING_COACH_WIDTH, DRAWING_COACH_HEIGHT);
    shapeRenderer.end();

    // Draw user's drawing
    shapeRenderer.begin(ShapeType.FilledRectangle);
    for (List<Vector2> pointSequence: pointSequences) {
      if (pointSequence.size() < 1) continue;
      prevPos.set(pointSequence.get(0));
      for (int i = 1; i < pointSequence.size(); i++) {
        pos.set(pointSequence.get(i)).sub(prevPos);
        shapeRenderer.translate(prevPos.x * ScienceEngine.PIXELS_PER_M, 
            prevPos.y * ScienceEngine.PIXELS_PER_M, 0);
        shapeRenderer.rotate(0, 0, 1, pos.angle());
        shapeRenderer.filledRect(0, 0, pos.len() * ScienceEngine.PIXELS_PER_M, LINE_WIDTH);
        shapeRenderer.rotate(0, 0, 1, -pos.angle());
        shapeRenderer.translate(-prevPos.x * ScienceEngine.PIXELS_PER_M, 
            -prevPos.y * ScienceEngine.PIXELS_PER_M, 0);
        prevPos.set(pointSequence.get(i));
      }
    }
    shapeRenderer.end();
  }

  public Actor getCoach() {
    return coach;
  }
}