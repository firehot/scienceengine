package com.mazalearn.scienceengine.domains.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Drawing;

public class DrawingActor extends Science2DActor {
  private static final int DRAW_WIDTH = 4;
  private static final float THUMBNAIL_SCALE = 2f;
  private final Drawing drawing;
  private Vector2 pos = new Vector2(), prevPos = new Vector2();
  private ShapeRenderer shapeRenderer;
  private BitmapFont font;
  private Image snapshotImage;
    
  public DrawingActor(Science2DBody body, TextureRegion textureRegion, BitmapFont font) {
    super(body, textureRegion);
    this.drawing = (Drawing) body;
    this.font = font;
    this.shapeRenderer = new ShapeRenderer();
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
          //drawing.setAngularVelocity(ANGULAR_VELOCITY);
          takeSnapshot();
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
  
  private void takeSnapshot() {
    Pixmap screenShot = ScreenUtils.getScreenshot((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight(), true);
    Pixmap thumbnail = ScreenUtils.createThumbnail(screenShot, THUMBNAIL_SCALE);
    snapshotImage = new Image(new TextureRegion(new Texture(thumbnail)));
    thumbnail.dispose();
    screenShot.dispose();
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    List<List<Vector2>> pointSequences = drawing.getPointSequences();
    if (pointSequences.size() < 1) {
      font.draw(batch, drawing.getExtra(), getX() + 20, getY() + 20);
    }
    batch.end();
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    shapeRenderer.identity();
    shapeRenderer.setColor(Color.WHITE);
    shapeRenderer.translate(getX(), getY(), 0);
    // Draw outline of allowed area for drawing
    if (pointSequences.size() == 0) {
      shapeRenderer.begin(ShapeType.Rectangle);
      shapeRenderer.rect(0, 0, getWidth(), getHeight());
      shapeRenderer.end();
    }

    for (List<Vector2> pointSequence: pointSequences) {
      if (pointSequence.size() < 1) continue;
      prevPos.set(pointSequence.get(0));
      for (int i = 1; i < pointSequence.size(); i++) {
        pos.set(pointSequence.get(i)).sub(prevPos);
        shapeRenderer.translate(prevPos.x * ScienceEngine.PIXELS_PER_M, 
            prevPos.y * ScienceEngine.PIXELS_PER_M, 0);
        shapeRenderer.rotate(0, 0, 1, pos.angle());
        shapeRenderer.begin(ShapeType.FilledRectangle);
        shapeRenderer.filledRect(0, 0, pos.len() * ScienceEngine.PIXELS_PER_M, DRAW_WIDTH);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeType.FilledCircle);
        shapeRenderer.filledCircle(0, 0, DRAW_WIDTH / 2f, 8);
        shapeRenderer.end();
        shapeRenderer.rotate(0, 0, 1, -pos.angle());
        shapeRenderer.translate(-prevPos.x * ScienceEngine.PIXELS_PER_M, 
            -prevPos.y * ScienceEngine.PIXELS_PER_M, 0);
        prevPos.set(pointSequence.get(i));
      }
    }
    batch.begin();
  }

  public Image getImage() {
    if (snapshotImage == null) {
      takeSnapshot();
    }
    return snapshotImage;
  }
}