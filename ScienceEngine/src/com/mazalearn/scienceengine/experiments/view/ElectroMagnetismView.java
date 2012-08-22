package com.mazalearn.scienceengine.experiments.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.IBody;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.Lightbulb;

public class ElectroMagnetismView extends Group implements IExperimentView {
  private final class BarMagnetView extends Box2DActor {
    private final ScienceBody body;

    private BarMagnetView(TextureRegion textureRegion, ScienceBody body) {
      super(body, textureRegion);
      this.body = body;
    }

    public boolean touchDown(float x, float y, int pointer) {
      return true;
    }

/*    public void touchDragged(float x, float y, int pointer) {
      body.setPositionAndAngle(x/PIXELS_PER_M, y/PIXELS_PER_M, body.getAngle());
      resume();
    } 
*/    
    public void touchUp(float x, float y, int pointer) {
      Vector2 newPos = new Vector2();
      newPos.set(x / PIXELS_PER_M, y / PIXELS_PER_M);
      newPos.sub(body.getPosition());
      float impulse = newPos.len() * 10f;
      //body.applyTorque(impulse);
      //body.applyForceToCenter(impulse, impulse);
      body.setAngularVelocity(impulse);
      resume();
    }
  }

  private final class LightbulbView extends Box2DActor {
    private final Lightbulb lightbulb;

    private LightbulbView(TextureRegion textureRegion, Lightbulb lightbulb) {
      super(lightbulb, textureRegion);
      this.lightbulb = lightbulb;
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
      super.draw(batch, parentAlpha);
      // Draw a circle of yellow light with radius and alpha proportional to intensity
      float intensity = lightbulb.getIntensity();
      int diameter = Math.round(intensity * 256);
      Pixmap pixmap = new Pixmap(diameter, diameter , Pixmap.Format.RGBA8888);
      pixmap.setColor(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, 0.40f + intensity * 0.5f);
      pixmap.fillCircle(diameter/2, diameter/2, diameter/2);
      TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
      pixmap.dispose();
      batch.draw(textureRegion, x + width/2 - diameter/2, y + height/2 - diameter/2);
    }
  }

  private static final int PIXELS_PER_M = 10;

  private boolean isPaused = false;
  
  private final ElectroMagnetismModel emModel;
  
  public ElectroMagnetismView(float width, float height, final ElectroMagnetismModel emModel) {
    this.width = width;
    this.height = height;
    this.emModel = emModel;
    for (final ScienceBody body: emModel.getBodies()) {
      TextureRegion textureRegion = getTextureRegionForBody(body.getName());
      if (body.getName() == "BarMagnet") {       
        this.addActor(new BarMagnetView(textureRegion, body));
      } else if (body.getName() == "Lightbulb") {
        this.addActor(new LightbulbView(textureRegion, (Lightbulb) body));
      } else {
        this.addActor(new Box2DActor(body, textureRegion));
      }
    }
  }
  
  private TextureRegion getTextureRegionForBody(String name) {
    Texture texture;
    if (name == "BarMagnet") {
      texture = new Texture("images/barmagnet-pivoted.png");
    } else if (name == "PickupCoil") {
      texture = new Texture("images/coppercoils.png");
    } else if (name == "Lightbulb") {
      texture = new Texture("images/lightbulb.png");
    } else {
      return null;
    }
    return new TextureRegion(texture);
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Advance n steps
    if (!isPaused ) {
      emModel.simulateStep();
    }
    super.draw(batch, parentAlpha);
  }

  @Override
  public void pause() {
    this.isPaused = true;
  }

  @Override
  public void resume() {
    this.isPaused = false;
  }

  @Override
  public boolean isPaused() {
    return isPaused;
  }
}
