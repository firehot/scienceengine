package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AnimateAction;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ElectroMagnet;

public class ElectromagnetActor extends Science2DActor {
  private static TextureRegion coil = 
      ScienceEngine.getTextureRegion("electromagnet-coil");
  private static float COIL_WIDTH = ElectroMagnet.DISPLAY_WIDTH;
  private ElectroMagnet electromagnet;
  public ElectromagnetActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    electromagnet = (ElectroMagnet) body;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // x is location between coils and front.
    // coil..coil..x..front
    float scale = electromagnet.getRadius() / ElectroMagnet.CANONICAL_RADIUS;
    batch.draw(getTextureRegion(), getX(), getY(), this.getOriginX(), 
        this.getOriginY(), super.getWidth() * scale, getHeight() * scale, 1, 1, getRotation());
    // Add the additional loops
    for (int i = 1; i <= electromagnet.getNumberOfLoops(); i++) {
      batch.draw(coil, getX() - i * ScreenComponent.getScaledX(COIL_WIDTH) * scale, getY(), 0, 0, super.getWidth() * scale, 
          getHeight() * scale, 1, 1, getRotation());
    }
    // Show current - direction is reversed
    CurrentSourceActor.drawCurrent(batch, electromagnet.getCurrent(), getX(),
        getY() + this.getHeight() * scale/2.35f);
  }
  
  @Override
  public float getWidth() {
    // TODO: Hack so that helptour can point to electromagnet correctly. Not used anywhere else.
    return super.getWidth() - (electromagnet != null ? electromagnet.getNumberOfLoops() * ScreenComponent.getScaledX(COIL_WIDTH) : 0);
  }

  public Actor hit (float x, float y, boolean touchable) {
    if (touchable && getTouchable() != Touchable.enabled) return null;
    return x >= - electromagnet.getNumberOfLoops() * ScreenComponent.getScaledX(COIL_WIDTH) && x < super.getWidth() 
        && y >= 0 && y < getHeight() ? this : null;
  }

  @Override
  public void showHelp(Stage stage, boolean animate) {
    if (animate) {
      addAction(AnimateAction.animatePosition(getX(), getY(), isVisible()));
    } else {
      clearActions();
    }
  }
}