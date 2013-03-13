package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.BField;

public class BFieldActor extends Science2DActor {
  private static final float X_SPACE = 150;
  private static final float Y_SPACE = 150;
  private final BField bField;
  private Vector2 pos = new Vector2();
  private TextureRegion textureUp;
  private TextureRegion textureDown;
  
  public BFieldActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.bField = (BField) body;
    this.removeListener(getListeners().get(0)); // help listener
    this.removeListener(getListeners().get(0)); // move, rotate listener
    this.textureUp = ScienceEngine.getTextureRegion("field-up");
    this.textureDown = ScienceEngine.getTextureRegion("field-down");
  }
  
  @Override
  public Actor hit (float x, float y, boolean touchable) {
    if (touchable && this.getTouchable() != Touchable.enabled) return null;
    // If nothing else hits, and fieldgrid is present, it shows a hit.
    // We exclude the top title and bottom status bars
    // Operate directly on input coords since x,y received here are wrt FieldMeter
    // and hence irrelevant
    getStage().screenToStageCoordinates(pos.set(Gdx.input.getX(), Gdx.input.getY()));
    return pos.y >= ScreenComponent.getScaledY(20) && 
        pos.y < ScreenComponent.VIEWPORT_HEIGHT - ScreenComponent.getScaledY(30) ? this : null;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Magnitude is scaled logarmthmically as width and height of arrow
    float magnitude = (float) Math.min(Math.log(1 + bField.getStrength()), 5);
    // find location of origin
    float width = magnitude * getWidth();
    float height = magnitude * getHeight();
    float originX = width / 2;
    float originY = height / 2;
    float bFieldZ = bField.getBFieldZ();
    float rotation =  bFieldZ == 0 ? (bField.getAngle() * MathUtils.radiansToDegrees) % 360 : 0;
    TextureRegion textureRegion = bFieldZ == 0 ? getTextureRegion() : (bFieldZ > 0 ? textureUp : textureDown);
    // Sample uniformly on X and Y axis and show fields at those points
    for (float x = 0; x < ScreenComponent.VIEWPORT_WIDTH; x += X_SPACE) {
      for (float y = 0; y < ScreenComponent.VIEWPORT_WIDTH; y += Y_SPACE) {
        pos.set(x - originX, y - originY);
        batch.draw(textureRegion, pos.x, pos.y, 
            originX, originY, width, height, 1, 1, rotation);
      }
    }
  }
}