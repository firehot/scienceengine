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
  private TextureRegion[] textureRegions;
  
  public FieldMeterActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.fieldMeter = (FieldMeter) body;
    this.textureRegions = new TextureRegion[] {
        ScienceEngine.getTextureRegion("field-down"),
        textureRegion,
        ScienceEngine.getTextureRegion("field-up")};
    
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

  public void draw1(SpriteBatch batch, float parentAlpha) {
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    batch.end();
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    List<FieldSample> fieldSamples = fieldMeter.getFieldSamples();
    shapeRenderer.setColor(Color.GREEN);

    // Reverse traversal - want to show the latest point first
    for (int i = fieldSamples.size() - 1; i >= 0; i--) {
      FieldSample fieldSample = fieldSamples.get(i);
      // Magnitude is scaled logarmthmically as width and height of arrow
      float magnitude = (float) Math.min(Math.log(1 + fieldSample.magnitude), 5);
      // location of field meter center is the sample point
      pos.set(fieldSample.x, fieldSample.y);
      pos.mul(ScreenComponent.PIXELS_PER_M);
      shapeRenderer.identity();
      shapeRenderer.translate(pos.x, pos.y, 0);
      // find half width and half height
      float w = magnitude * getWidth() / 2;
      float h = magnitude * getHeight() / 2;
      // Bottom of arrow position
      float rotation =  (fieldSample.theta * MathUtils.radiansToDegrees) % 360;
      shapeRenderer.rotate(0, 1, 0, rotation);
      shapeRenderer.begin(ShapeType.Box);
      shapeRenderer.box(-w, -h, -h, w, h, h);
      shapeRenderer.end();
      shapeRenderer.translate(0, 0, 0);
      shapeRenderer.begin(ShapeType.FilledCone);
      shapeRenderer.rotate(0, 1, 0, 90);
      shapeRenderer.filledCone(w, 0, 0, h, h/3f);
      shapeRenderer.end();
    }
    batch.begin();
  }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
    List<FieldSample> fieldSamples = fieldMeter.getFieldSamples();
    // Reverse traversal - want to show the latest point first
    for (int i = fieldSamples.size() - 1; i >= 0; i--) {
      FieldSample fieldSample = fieldSamples.get(i);
      // Magnitude is scaled logarmthmically as width and height of arrow
      float magnitude = (float) Math.min(Math.log(1 + fieldSample.magnitude), 5);
      // location of field meter center is the sample point
      pos.set(fieldSample.x, fieldSample.y);
      pos.mul(ScreenComponent.PIXELS_PER_M);
      // find location of origin
      float originX = magnitude * getWidth() / 2;
      float originY = magnitude * getHeight() / 2;
      // Bottom of arrow position
      pos.sub(originX, originY);
      float rotation =  (fieldSample.theta * MathUtils.radiansToDegrees) % 360;
      TextureRegion textureRegion = textureRegions[fieldSample.phi + 1];
      batch.draw(textureRegion, pos.x, pos.y, 
          originX, originY, getWidth() * magnitude, getHeight() * magnitude, 1, 1, rotation);
    }
  }
}