package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass.FieldSample;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentWire;

public class CurrentWireView extends Box2DActor {
  private final CurrentWire currentWire;
  private float radius;
    
  public CurrentWireView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.radius = (float) Math.sqrt(width * width + height * height)/2;
    this.currentWire = (CurrentWire) body;
    this.originX = width/2;
    this.originY = height/2;
  }
}