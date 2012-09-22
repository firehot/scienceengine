package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.box2d.ScienceActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.box2d.ScienceBody.ComponentType;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.BarMagnetView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CompassView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentSourceView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentWireView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.ElectroMagnetView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.FieldMeterView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.LightbulbView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.PickupCoilView;
import com.mazalearn.scienceengine.services.SoundManager;
import com.mazalearn.scienceengine.view.AbstractExperimentView;
import com.mazalearn.scienceengine.view.ProbeManager;

public class ElectroMagnetismView extends AbstractExperimentView {
  private BarMagnetView barMagnetView;
  private ProbeManager probeManager;
  private boolean isFieldPointTouched = false;
  private FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
  private ElectroMagnetismModel emModel;

  public ElectroMagnetismView(String experimentName, float width, float height,
      final ElectroMagnetismModel emModel,
      Skin skin, SoundManager soundManager) {
    super(experimentName, emModel, width, height, skin, soundManager);
    this.emModel = emModel;
    
    Actor lightbulb = null, pickupCoil = null;
    ScienceActor electroMagnet = null, currentSource = null;
    Actor coilsBack = new Image(new Texture("images/coppercoils-back.png"),
        Scaling.stretch, Align.CENTER, "CoilsBack");
    this.addActor(coilsBack);
    for (final ScienceBody body: emModel.getBodies()) {
      TextureRegion textureRegion = getTextureRegionForBody(body.getComponentType());
      if (textureRegion == null) continue;
      switch (body.getComponentType()) {
      case BarMagnet:       
        barMagnetView = new BarMagnetView(textureRegion, body, this, emModel);
        this.addActor(barMagnetView);
        break;
      case Lightbulb:
        this.addActor(lightbulb = new LightbulbView(textureRegion, (Lightbulb) body));
        break;
      case PickupCoil:
        this.addActor(pickupCoil = new PickupCoilView(body, textureRegion));
        break;
      case Compass:
        this.addActor(new CompassView(textureRegion, body));
        break;
      case FieldMeter:
        this.addActor(new FieldMeterView(textureRegion, body));
        fieldMeter = (FieldMeter) body;
        break;
      case CurrentSource:
        this.addActor(currentSource = new CurrentSourceView(body, textureRegion));
        break;        
      case CurrentWire:
        this.addActor(new CurrentWireView(body));
        break;
      case ElectroMagnet:
        this.addActor(electroMagnet = new ElectroMagnetView(textureRegion, body, this, emModel));
        break;
      default:
        this.addActor(new ScienceActor(body, textureRegion));
        break;
      }
    }
    addLocationGroup(coilsBack, pickupCoil, lightbulb);
    addLocationGroup(currentSource, electroMagnet);
  }
  
  // TODO: Use texture Atlas for this
  private TextureRegion getTextureRegionForBody(ComponentType componentType) {
    Texture texture;
    switch(componentType) {
    case ElectroMagnet:
      texture = new Texture("images/electromagnet.png");
      break;
    case BarMagnet:
      texture = new Texture("images/barmagnet-pivoted.png");
      break;
    case PickupCoil:
      texture = new Texture("images/coppercoils-front.png");
      break;
    case Lightbulb:
      texture = new Texture("images/lightbulb.png");
      break;
    case Compass:
      texture = new Texture("images/compass.png");
      break;
    case FieldMeter:
      texture = new Texture("images/arrow.png");
      break;
    case CurrentWire:
      texture = new Texture("images/currentwire-up.png");
      break;
    case CurrentSource:
      texture = new Texture("images/currentsource.png");
      break;
    default:
      return null;
    }
    return new TextureRegion(texture);
  }
  
  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    super.touchDown(x, y,  pointer, button);
    Actor touchedActor = super.getTouchFocus(pointer);
    if (!(touchedActor instanceof ScienceActor) && touchedActor != null) return true;
    // Touch at stage level - not on any actor - Assume field touch
    if (fieldMeter.isActive()) isFieldPointTouched = true;
    return true;
  }

  @Override
  public boolean touchUp(int x, int y, int pointer, int button) {
    super.touchUp(x, y, pointer, button);
    if (fieldMeter.isActive() && isFieldPointTouched) {
      isFieldPointTouched = false;
      // Move field sampler here.
      // view coords
      toStageCoordinates(x, y, pos);
      // model coords
      pos.mul(1f / ScienceEngine.PIXELS_PER_M);
      fieldMeter.setPositionAndAngle(pos, 0);
    }
    return true;
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
      probeManager.setTitle("Congratulations! You move to Level " + level);
      challenge(false);
      getLevelManager().setLevel(level);
      getLevelManager().load();
    }
  }
}
