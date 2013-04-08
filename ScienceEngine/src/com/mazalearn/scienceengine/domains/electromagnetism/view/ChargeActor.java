package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Charge;

public class ChargeActor extends Science2DActor {
  protected static final float SCALE = -0.05f;
  private final Charge charge;
  Vector2 lastTouch = new Vector2(), currentTouch = new Vector2();
  private static TextureRegion textureNegative = ScienceEngine.getTextureRegion("negative");;
  private static TextureRegion texturePositive = ScienceEngine.getTextureRegion("positive");
    
  public ChargeActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, new TextureRegion(textureRegion));
    this.charge = (Charge) body;
    this.removeListener(getListeners().get(0)); // help listener
    this.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        lastTouch.set(event.getStageX(), event.getStageY());
        return true;
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        // Get negative of movement vector
        lastTouch.sub(event.getStageX(), event.getStageY()).mul(SCALE);
        charge.setLinearVelocity(lastTouch);
      }
    });
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    // Wrap around
    if (getX() < 0 || getX() > ScreenComponent.VIEWPORT_WIDTH || 
        getY() < 0 || getY() > ScreenComponent.VIEWPORT_HEIGHT) {
      setX((getX() + ScreenComponent.VIEWPORT_WIDTH) % ScreenComponent.VIEWPORT_WIDTH);
      setY((getY() + ScreenComponent.VIEWPORT_HEIGHT) % ScreenComponent.VIEWPORT_HEIGHT);
      this.setPositionFromViewCoords(false);
    }
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.setTextureRegion(
        charge.getStrength() >= 0 ? texturePositive : textureNegative);
    super.draw(batch, parentAlpha);
  }
}