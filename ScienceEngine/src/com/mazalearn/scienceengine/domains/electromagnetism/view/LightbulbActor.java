package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;

public class LightbulbActor extends Science2DActor {
  private final Lightbulb lightbulb;
  private static TextureRegion LIGHT_TEXTURE = createLightTexture();
  private float lightRadius;
  private Vector2 point = new Vector2();

  public LightbulbActor(Lightbulb lightbulb, TextureRegion textureRegion) {
    super(lightbulb, textureRegion);
    this.lightbulb = lightbulb;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    float intensity = Math.abs(lightbulb.getIntensity());
    int scale = Gdx.graphics.getFramesPerSecond() < 10 ? 512 : 128;
    float diameter = scale * intensity;
    drawLight(batch, intensity, diameter, lightbulb.getColor(),
        getX() + getOriginX(), getY() + getOriginY());
    lightRadius = diameter / 2;
    super.draw(batch, parentAlpha);
  }

  // Package protected, also used by DrawingActor.Coach
  // Draw a circle of light with radius and alpha proportional to intensity
  static void drawLight(SpriteBatch batch, float intensity, float diameter, 
      Color color, float x, float y) {
    int radius = Math.round(diameter / 2);
    Color c = batch.getColor();
    batch.setColor(color.r, color.g, color.b, 0.5f + intensity * 0.5f);
    batch.draw(LIGHT_TEXTURE, x - radius, y - radius, diameter, diameter);
    batch.setColor(c);
  }

  private static TextureRegion createLightTexture() {
    Pixmap pixmap = new Pixmap(256, 256 , Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
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