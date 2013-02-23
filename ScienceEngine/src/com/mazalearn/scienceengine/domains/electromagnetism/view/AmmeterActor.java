package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Ammeter;

public class AmmeterActor extends Science2DActor {
  private final Ammeter ammeter;
  private ShapeRenderer shapeRenderer;   
    
  public AmmeterActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.ammeter = (Ammeter) body;
    this.shapeRenderer = new ShapeRenderer();
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    batch.end();
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    shapeRenderer.identity();
    shapeRenderer.translate(getX() + getOriginX() + 1, getY() + 10, 0);
    shapeRenderer.setColor(Color.DARK_GRAY);
    shapeRenderer.begin(ShapeType.Line);
    float min = MathUtils.sin(ammeter.getMinNeedleAngle()) * getHeight() * 0.5f;
    float max = MathUtils.sin(ammeter.getMaxNeedleAngle()) * getHeight() * 0.5f;
    shapeRenderer.line(0, 0, min, getHeight() * 0.50f);
    shapeRenderer.line(0, 0, max, getHeight() * 0.50f);
    shapeRenderer.end();
    shapeRenderer.begin(ShapeType.FilledRectangle);
    shapeRenderer.setColor(Color.RED);
    float needleAngle = ammeter.getNeedleAngle() * MathUtils.radiansToDegrees;
    shapeRenderer.rotate(0, 0, 1, 90 + needleAngle);
    shapeRenderer.filledRect(0, 0, getHeight() * 0.75f, 2);
    shapeRenderer.end();
    batch.begin();
  }
}