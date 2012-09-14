package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.BarMagnetView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CompassView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentWireView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.FreeNorthPoleView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.LightbulbView;
import com.mazalearn.scienceengine.services.SoundManager;
import com.mazalearn.scienceengine.view.AbstractExperimentView;
import com.mazalearn.scienceengine.view.ProbeManager;

public class ElectroMagnetismView extends AbstractExperimentView {
  private BarMagnetView barMagnetView;
  private ProbeManager probeManager;

  public ElectroMagnetismView(String experimentName, float width, float height,
      final ElectroMagnetismModel emModel, Skin skin, SoundManager soundManager) {
    super(experimentName, emModel, width, height, skin, soundManager);
    
    // TODO: use blending function to draw coilsback?
    Actor coilsBack = new Image(new Texture("images/coppercoils-back.png"),
        Scaling.stretch, Align.CENTER, "CoilsBack");
    this.addActor(coilsBack);
    for (final ScienceBody body: emModel.getBodies()) {
      TextureRegion textureRegion = getTextureRegionForBody(body.getName());
      if (body.getName() == "BarMagnet") {       
        barMagnetView = new BarMagnetView(textureRegion, body, this, emModel);
        this.addActor(barMagnetView);
      } else if (body.getName() == "Lightbulb") {
        this.addActor(new LightbulbView(textureRegion, (Lightbulb) body));
      } else if (body.getName() == "PickupCoil") {
        coilsBack.x = body.getPosition().x * PIXELS_PER_M;
        coilsBack.y = body.getPosition().y * PIXELS_PER_M;
        this.addActor(new Box2DActor(body, textureRegion));
      } else if (body.getName() == "Compass") {
        this.addActor(new CompassView(textureRegion, body));
      } else if (body.getName() == "FreeNorthPole") {
        this.addActor(new FreeNorthPoleView(textureRegion, body));
      } else if (body.getName() == "CurrentWire") {
        this.addActor(new CurrentWireView(textureRegion, body));
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
      texture = new Texture("images/coppercoils-front.png");
    } else if (name == "Lightbulb") {
      texture = new Texture("images/lightbulb.png");
    } else if (name == "Compass") {
      texture = new Texture("images/compass.png");
    } else if (name == "FreeNorthPole") {
      texture = new Texture("images/freenorthpole.png");
    } else if (name == "CurrentWire") {
      texture = new Texture("images/wireUp.png");
    } else {
      return null;
    }
    return new TextureRegion(texture);
  }
  
  @Override
  public void challenge(boolean challenge) {
    super.challenge(challenge);
    if (challenge) {
      if (probeManager == null) {
        probeManager = new ProbeManager(skin, width, height, this, soundManager); 
        this.addActor(probeManager);
        probeManager.add(new FieldDirectionProber(skin, barMagnetView, probeManager));
        probeManager.add(new FieldMagnitudeProber(skin, barMagnetView, probeManager));
      }
      probeManager.startChallenge();
    }
    probeManager.visible = challenge;
  };
  
  public void done(boolean success) {
    if (success) {
      int level = getLevelManager().getLevel() + 1;
      probeManager.setTitle("Congratulations! You move to Level " + 
          String.valueOf(level));
      challenge(false);
      getLevelManager().setLevel(level);
      getLevelManager().load();
    }
  }
}
