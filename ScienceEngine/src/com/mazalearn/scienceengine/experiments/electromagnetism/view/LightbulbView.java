package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;

public class LightbulbView extends Box2DActor {
  private final Lightbulb lightbulb;
  private TextureRegion lightTexturePositive, lightTextureNegative;

  public LightbulbView(TextureRegion textureRegion, Lightbulb lightbulb) {
    super(lightbulb, textureRegion);
    this.lightbulb = lightbulb;
    lightTexturePositive = createLightTexture(Color.YELLOW);
    lightTextureNegative = createLightTexture(Color.BLUE);
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);

    float intensity = Math.abs(lightbulb.getIntensity());
    // Draw a circle of yellow light with radius and alpha proportional to intensity
    int diameter = Math.round(intensity * 256);
    Color c = batch.getColor();
    batch.setColor(1, 1, 1, 0.5f + intensity * 0.5f);
    TextureRegion t = lightbulb.getIntensity() > 0 ? lightTexturePositive : lightTextureNegative;
    batch.draw(t, x + width/2 - diameter/2, y + height/2 - diameter/2, diameter, diameter);
    batch.setColor(c);
  }

  private TextureRegion createLightTexture(Color color) {
    Pixmap pixmap = new Pixmap(256, 256 , Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fillCircle(256/2, 256/2, 256/2);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return textureRegion;
  }
}