package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;

public class LightbulbActor extends Science2DActor {
  private final Lightbulb lightbulb;
  private TextureRegion lightTexture;
  private float lightRadius;
  private Vector2 point = new Vector2();

  public LightbulbActor(TextureRegion textureRegion, Lightbulb lightbulb) {
    super(lightbulb, textureRegion);
    this.lightbulb = lightbulb;
    lightTexture = createLightTexture(Color.YELLOW);
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    float intensity = Math.abs(lightbulb.getIntensity());
    // Draw a circle of yellow light with radius and alpha proportional to intensity
    int diameter = Math.round(intensity * 256);
    lightRadius = diameter / 2;
    Color c = batch.getColor();
    batch.setColor(1, 1, 1, 0.5f + intensity * 0.5f);
    batch.draw(lightTexture, getX() + getOriginX() - lightRadius, 
        getY() + getOriginY() - lightRadius, diameter, diameter);
    batch.setColor(c);
    super.draw(batch, parentAlpha);
  }

  private TextureRegion createLightTexture(Color color) {
    Pixmap pixmap = new Pixmap(256, 256 , Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fillCircle(256/2, 256/2, 256/2);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return textureRegion;
  }
  
  public boolean withinLightRegion(float x, float y) {
    point.set(this.getX() + getOriginX(), this.getY() + getOriginY()).sub(x, y);
    return point.len() <= lightRadius;
  }

}