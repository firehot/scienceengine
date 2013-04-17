package com.mazalearn.scienceengine.domains.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter.FieldSample;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter.SampleMode;

public class FieldMeterActor extends Science2DActor {
  private final FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
  
  public FieldMeterActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.fieldMeter = (FieldMeter) body;
    
    this.removeListener(getListeners().get(0)); // help listener
    this.removeListener(getListeners().get(0)); // move, rotate listener
    
    this.addListener(new ClickListener() {      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (fieldMeter.getSampleMode() == SampleMode.Uniform) return;
        super.touchUp(event, x, y, pointer, button);
        ScienceEngine.selectBody(fieldMeter, (IScience2DView) getStage());
        // Move field sampler here and convert to model coords
        pos.set(event.getStageX(), event.getStageY()).mul(1f / ScreenComponent.PIXELS_PER_M);
        fieldMeter.setPositionAndAngle(pos, 0);
      }
    });
  }
  
  @Override
  public Actor hit (float x, float y, boolean touchable) {
    if (touchable && this.getTouchable() != Touchable.enabled) return null;
    // If nothing else hits, and fieldmeter is present, it shows a hit.
    return this;
  }
  
  @Override
  public void showHelp(Stage stage, boolean animate) {
    if (animate) {
      pos.set(getX(), getY()).mul(1f / ScreenComponent.PIXELS_PER_M);
      fieldMeter.setPositionAndAngle(pos, 0);
    } else {
      fieldMeter.reset();
    }
    super.showHelp(stage, animate);
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    batch.end();
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    List<FieldSample> fieldSamples = fieldMeter.getFieldSamples();

    // Reverse traversal - want to show the latest point first
    for (int i = fieldSamples.size() - 1; i >= 0; i--) {
      FieldSample fieldSample = fieldSamples.get(i);
      // Magnitude is scaled logarmthmically as width and height of arrow
      float magnitude = (float) Math.min(Math.log(1 + fieldSample.magnitude), 3);
      // location of field meter center is the sample point
      pos.set(fieldSample.x, fieldSample.y);
      pos.mul(ScreenComponent.PIXELS_PER_M);
      shapeRenderer.identity();
      shapeRenderer.translate(pos.x, pos.y, 0);
      // find half width and half height
      float w = magnitude * getWidth() / 3;
      float h = magnitude * getHeight() / 5;
      // Bottom of arrow position
      float theta =  (fieldSample.theta * MathUtils.radiansToDegrees) % 360;
      float phi = fieldSample.phi * MathUtils.radiansToDegrees;
      // TODO: animated change to z-direction field
      if (phi == 0) {
        shapeRenderer.rotate(0, 0, 1, theta);
//        shapeRenderer.rotate(0, 1, 0, phi);
        shapeRenderer.setColor(ScienceEngine.getSkin().getColor("field"));
        shapeRenderer.begin(ShapeType.FilledRectangle);
        shapeRenderer.filledRect(-w - w / 2, -h, 2 * w, 2 * h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeType.FilledTriangle);
        shapeRenderer.filledTriangle(w - w/2, -2 * h, w - w/2, 2 * h, w + w, 0);
        shapeRenderer.end();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeType.FilledCircle);
        shapeRenderer.filledCircle(0, 0, magnitude);
        shapeRenderer.end();
//        shapeRenderer.rotate(0, 1, 0, -phi);
        shapeRenderer.rotate(0, 0, 1, -theta);
      } else 
      if (phi > 0) {
        shapeRenderer.setColor(ScienceEngine.getSkin().getColor("field"));
        shapeRenderer.begin(ShapeType.FilledCircle);
        shapeRenderer.filledCircle(0, 0, h);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.filledCircle(0, 0, magnitude);
        shapeRenderer.end();
      } else if (phi < 0) {
        shapeRenderer.setColor(ScienceEngine.getSkin().getColor("field"));
        shapeRenderer.begin(ShapeType.FilledRectangle);
        shapeRenderer.rotate(0, 0, 1, 45);
        shapeRenderer.filledRect(-h*2, -magnitude/2, 4 * h, magnitude);
        shapeRenderer.filledRect(-magnitude/2, -h*2, magnitude, 4 * h);
        shapeRenderer.end();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeType.FilledCircle);
        shapeRenderer.filledCircle(0, 0, magnitude);
        shapeRenderer.end();
      }
    }
    batch.begin();
  }
}