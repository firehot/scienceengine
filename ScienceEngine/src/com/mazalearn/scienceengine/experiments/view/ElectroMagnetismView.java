package com.mazalearn.scienceengine.experiments.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel.Mode;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.Lightbulb;

public class ElectroMagnetismView extends Group implements IExperimentView {
  private static final class BarMagnetView extends Box2DActor {
    private final ScienceBody body;
    private final ElectroMagnetismView emView;
    private final ElectroMagnetismModel emModel;
    private BarMagnetView(TextureRegion textureRegion, ScienceBody body, 
        ElectroMagnetismView experimentView, ElectroMagnetismModel emModel) {
      super(body, textureRegion);
      this.body = body;
      this.emView = experimentView;
      this.emModel = emModel;
    }

    public boolean touchDown(float x, float y, int pointer) {
      return true;
    }

    public void touchDragged(float x, float y, int pointer) {
      if (Mode.valueOf(emModel.getMode()) != Mode.Free) return;
      body.setPositionAndAngle(x/PIXELS_PER_M, y/PIXELS_PER_M, body.getAngle());
      emView.resume();
    } 
    
    public void touchUp(float x, float y, int pointer) {
      if (Mode.valueOf(emModel.getMode()) != Mode.Rotate) return; 
      Vector2 newPos = new Vector2();
      newPos.set(x / PIXELS_PER_M, y / PIXELS_PER_M);
      newPos.sub(body.getPosition());
      float angularVelocity = newPos.len();
      body.setAngularVelocity(angularVelocity);
      emView.resume();
    }
    
    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
      this.x = body.getPosition().x * PIXELS_PER_M;
      this.y = body.getPosition().y * PIXELS_PER_M;
      if (body.getAngle() != 0) {
        double angle = body.getAngle() * MathUtils.radiansToDegrees;
        this.rotation = (float) (angle % 360);
      }
      batch.draw(getTextureRegion(), x - width/2, y - height/2, width/2, height/2, width, height, 1, 1, rotation);
    }

    @Override
    public Actor hit(float x, float y) {
      // x,y are in in actor's coordinates
      return x > -width/2 && x < width/2 && y > -height/2 && y < height/2 ? this : null;
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
      float intensity = Math.abs(lightbulb.getIntensity());
      super.draw(batch, parentAlpha);
      // Draw a circle of yellow light with radius and alpha proportional to intensity
      int diameter = Math.round(intensity * 256);
      Color c = batch.getColor();
      batch.setColor(1, 1, 1, 0.5f + intensity * 0.5f);
      TextureRegion t = lightbulb.getInertia() > 0 ? lightTexturePositive : lightTextureNegative;
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

  private static final int PIXELS_PER_M = 8;

  private boolean isPaused = false;
  
  private final ElectroMagnetismModel emModel;

  public ElectroMagnetismView(float width, float height, final ElectroMagnetismModel emModel) {
    this.width = width;
    this.height = height;
    this.emModel = emModel;
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
