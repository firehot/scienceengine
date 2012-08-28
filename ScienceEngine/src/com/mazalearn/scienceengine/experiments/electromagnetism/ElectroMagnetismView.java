package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel.Mode;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class ElectroMagnetismView extends AbstractExperimentView {
  private static final class BarMagnetView extends Box2DActor {
    private final BarMagnet barMagnet;
    private final ElectroMagnetismView emView;
    private final ElectroMagnetismModel emModel;
    private BitmapFont font;
    private Vector2 touchDownPos = new Vector2();
    private Vector2 newPos = new Vector2();
    private TextureRegion textureRegion;
    
    private BarMagnetView(TextureRegion textureRegion, ScienceBody body, 
        ElectroMagnetismView experimentView, ElectroMagnetismModel emModel) {
      super(body, textureRegion);
      this.barMagnet = (BarMagnet) body;
      this.textureRegion = textureRegion;
      this.emView = experimentView;
      this.emModel = emModel;
      this.font = new BitmapFont();
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
      // Add displacement vector to the actor position to find new position
      newPos.add(this.x, this.y);
      // Find center of bar Magnet in new position
      newPos.add(width/2, height/2);
      // Scale down from actor coords to barMagnet coords
      newPos.mul(1f/PIXELS_PER_M);
      // Move barMagnet to new position
      barMagnet.setPositionAndAngle(newPos, barMagnet.getAngle());
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
      touchDownPos.sub(width/2, height/2);
      touchDownPos.mul(1f/PIXELS_PER_M);
      barMagnet.applyForce(newPos, barMagnet.getWorldPoint(touchDownPos));
      emView.resume();
    }
    
    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
      // Find view position of left bottom corner of bar Magnet
      newPos.set(-barMagnet.getWidth()/2, -barMagnet.getHeight()/2);
      newPos.set(barMagnet.getWorldPoint(newPos));
      newPos.mul(PIXELS_PER_M);
      this.x = newPos.x;
      this.y = newPos.y;
      this.rotation = (barMagnet.getAngle() * MathUtils.radiansToDegrees) % 360;
      batch.draw(textureRegion, x, y, 0, 0, width, height, 1, 1, rotation);

      if (Mode.valueOf(emModel.getMode()) == Mode.Rotate) { // Display RPM
        font.setColor(1f, 1f, 1f, parentAlpha);
        String rpm = String.valueOf(Math.floor(barMagnet.getAngularVelocity()));
        newPos.set(barMagnet.getWorldCenter());
        newPos.mul(PIXELS_PER_M);
        newPos.add(-10, 5);
        font.draw(batch, rpm, newPos.x, newPos.y);
      }
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
