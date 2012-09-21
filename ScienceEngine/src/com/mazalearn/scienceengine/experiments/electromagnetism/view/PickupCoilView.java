package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;

public class PickupCoilView extends Box2DActor {
  private final Actor coilsBack;

  public PickupCoilView(ScienceBody body, TextureRegion textureRegion,
      Actor coilsBack) {
    super(body, textureRegion);
    this.coilsBack = coilsBack;
    this.setAllowDrag(true);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    coilsBack.x = x;
    coilsBack.y = y;
  }
}