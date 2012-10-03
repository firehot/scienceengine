package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.model.Science2DBody.ComponentType;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.FieldDirectionProber;
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.FieldMagnitudeProber;
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.LightProber;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.BarMagnetActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentSourceActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentWireActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.ElectromagnetActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.FieldMeterActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.PickupCoilActor;

public class ElectroMagnetismView extends AbstractScience2DStage {
  private BarMagnetActor barMagnetActor;
  private ProbeManager probeManager;
  private FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
  private ElectroMagnetismModel emModel;
  private FieldMeterActor fieldMeterActor;
  private Science2DActor compassActor;

  public ElectroMagnetismView(float width, float height,
      final ElectroMagnetismModel emModel, Skin skin) {
    super(emModel, width, height, skin);
    this.emModel = emModel;
    
    Actor lightbulb = null, pickupCoil = null;
    Science2DActor electroMagnet = null, currentSource = null;
    Actor coilsBack = new Image(new Texture("images/coppercoils-back.png"));
    coilsBack.setName("CoilsBack");
    this.addActor(coilsBack);
    for (final Science2DBody body: emModel.getBodies()) {
      TextureRegion textureRegion = getTextureRegionForBody(body.getComponentType());
      if (textureRegion == null) continue;
      switch (body.getComponentType()) {
      case BarMagnet:       
        barMagnetActor = new BarMagnetActor(textureRegion, body, this, emModel);
        this.addActor(barMagnetActor);
        break;
      case Lightbulb:
        this.addActor(lightbulb = new LightbulbActor(textureRegion, (Lightbulb) body));
        break;
      case PickupCoil:
        this.addActor(pickupCoil = new PickupCoilActor(body, textureRegion));
        break;
      case FieldMeter:
        this.addActor(fieldMeterActor = new FieldMeterActor(textureRegion, body));
        fieldMeter = (FieldMeter) body;
        break;
      case CurrentSource:
        this.addActor(currentSource = new CurrentSourceActor(body, textureRegion));
        break;        
      case CurrentWire:
        this.addActor(new CurrentWireActor(body));
        break;
      case ElectroMagnet:
        this.addActor(electroMagnet = new ElectromagnetActor(body, textureRegion));
        break;
      case Compass:
        this.addActor(compassActor = new Science2DActor(body, textureRegion));
        break;
      default:
        this.addActor(new Science2DActor(body, textureRegion));
        break;
      }
    }
    addLocationGroup(coilsBack, pickupCoil, lightbulb);
    addLocationGroup(currentSource, electroMagnet);
    
    getRoot().addListener(new ClickListener() {
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        if (fieldMeter.isActive() && event.getTarget() == getRoot()) {
          // Move field sampler here and convert to model coords
          pos.set(x, y).mul(1f / ScienceEngine.PIXELS_PER_M);
          fieldMeter.setPositionAndAngle(pos, 0);
        }
      }       
    });
  }
  
  // TODO: Use texture Atlas for this
  private TextureRegion getTextureRegionForBody(ComponentType componentType) {
    Texture texture;
    switch(componentType) {
    case ElectroMagnet:
      texture = new Texture("images/electromagnet-base.png");
      break;
    case BarMagnet:
      texture = new Texture("images/barmagnet-pivoted.png");
      break;
    case PickupCoil:
      texture = new Texture("images/coppercoils-front1.png");
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
  public void challenge(boolean challenge) {
    super.challenge(challenge);
    // Enable/Disable field meter, compass
    compassActor.setVisible(!challenge);
    if (probeManager == null) {
      probeManager = new ProbeManager(skin, getWidth(), getHeight(), 
          controlPanel.getModelConfigs(), this, controlPanel);        
      new FieldDirectionProber(emModel, probeManager);
      new FieldMagnitudeProber(emModel, probeManager);
      new LightProber(probeManager);
      this.getRoot().addActorBefore(fieldMeterActor, probeManager);     
    }
    if (challenge) {
      probeManager.startChallenge();
    }
    probeManager.setVisible(challenge);
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
