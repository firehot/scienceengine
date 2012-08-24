package com.mazalearn.scienceengine.experiments.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel.Mode;
import com.mazalearn.scienceengine.experiments.model.IExperimentModel;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.Lightbulb;

public class ElectroMagnetismView extends AbstractExperimentView {
  private static final class BarMagnetView extends Box2DActor {
    private final ScienceBody body;
    private final ElectroMagnetismView emView;
    private final ElectroMagnetismModel emModel;
    private Vector2 touchDownPos = new Vector2();
    private Vector2 newPos = new Vector2();
    private BarMagnetView(TextureRegion textureRegion, ScienceBody body, 
        ElectroMagnetismView experimentView, ElectroMagnetismModel emModel) {
      super(body, textureRegion);
      this.body = body;
      this.emView = experimentView;
      this.emModel = emModel;
    }

    public boolean touchDown(float x, float y, int pointer) {
      touchDownPos.set(x, y);
      return true;
    }

    public void touchDragged(float x, float y, int pointer) {
      if (Mode.valueOf(emModel.getMode()) != Mode.Free) return;
      // New touch position
      newPos.set(x, y);
      // Subtract old touch position to get displacement vector
      newPos.sub(touchDownPos);
      // Add displacement vector to the actor position
      newPos.add(this.x, this.y);
      // Scale down from actor coords to body coords
      newPos.mul(1f/PIXELS_PER_M);
      // Move body to new position
      body.setPositionAndAngle(newPos, body.getAngle());
      emView.resume();
    } 
    
    public void touchUp(float x, float y, int pointer) {
      if (Mode.valueOf(emModel.getMode()) != Mode.Rotate) return;
      // new touch position
      newPos.set(x, y);
      // Subtract old touch position to get displacement vector
      newPos.sub(touchDownPos);
      // Scale displacement vector suitably to get a proportional force
      newPos.mul(2000);
      // Apply the force at point touched in world coords
      body.applyForce(newPos, body.getWorldPoint(touchDownPos));
      emView.resume();
    }
  }

  private static final class LightbulbView extends Box2DActor {
    private final Lightbulb lightbulb;
    private TextureRegion lightTexturePositive, lightTextureNegative;

    private LightbulbView(TextureRegion textureRegion, Lightbulb lightbulb) {
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

  private IExperimentModel emModel;
  
  public ElectroMagnetismView(float width, float height, final ElectroMagnetismModel emModel) {
    super(emModel);
    this.emModel = emModel;
    this.width = width;
    this.height = height;
    for (final ScienceBody body: emModel.getBodies()) {
      TextureRegion textureRegion = getTextureRegionForBody(body.getName());
      if (body.getName() == "BarMagnet") {       
        this.addActor(new BarMagnetView(textureRegion, body, this, emModel));
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
      emModel.simulateSteps(1);
    }
    super.draw(batch, parentAlpha);
  }
}
