package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.List;

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
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.FieldDirectionProber;
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.FieldMagnitudeProber;
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.LightProber;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.BarMagnetActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CircuitActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentCoilActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentSourceActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CurrentWireActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.ElectromagnetActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.FieldMeterActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.PickupCoilActor;

public class ElectroMagnetismView extends AbstractScience2DStage {
  private ProbeManager probeManager;
  private FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
  private ElectroMagnetismModel emModel;
  private FieldMeterActor fieldMeterActor;
  private Actor compassActor;

  public ElectroMagnetismView(float width, float height,
      final ElectroMagnetismModel emModel, Skin skin) {
    super(emModel, width, height, skin);
    this.emModel = emModel;
    
    getRoot().addListener(new ClickListener() {
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        if (fieldMeter != null && fieldMeter.isActive() && event.getTarget() == getRoot()) {
          // Move field sampler here and convert to model coords
          pos.set(x, y).mul(1f / ScienceEngine.PIXELS_PER_M);
          fieldMeter.setPositionAndAngle(pos, 0);
        }
      }       
    });

    Actor coilsBack = new Image(new Texture("images/coppercoils-back.png"));
    coilsBack.setName("CoilsBack");
    Actor brushes = new Image(new Texture("images/brush.png"));
    brushes.setName("Brushes");
    this.addActor(coilsBack);
    this.addActor(brushes);
    for (Science2DBody body: emModel.getBodies()) {
      Actor actor = createActor(emModel, body);
      if (actor != null) {
        this.addActor(actor);
      }
    }
    
    for (List<CircuitElement> circuit: science2DModel.getCircuits()) {
      this.addActor(new CircuitActor(circuit));
    }

    fieldMeter = (FieldMeter) emModel.findBody(ComponentType.FieldMeter);
    compassActor = findActor(ComponentType.Compass.name());
    
  }

  private Actor createActor(ElectroMagnetismModel emModel, Science2DBody body) {
    ComponentType componentType = ComponentType.valueOf(body.getComponentType());
    
    String textureFilename = componentType.getTextureFilename();
    if (textureFilename == null) return null;

    TextureRegion textureRegion = 
        new TextureRegion(new Texture(textureFilename));
    
    switch (componentType) {
    case BarMagnet:
      return new BarMagnetActor(body, textureRegion, getFont());
    case Lightbulb:
      return new LightbulbActor((Lightbulb) body, textureRegion);
    case PickupCoil:
      return new PickupCoilActor((PickupCoil) body, textureRegion);
    case FieldMeter:
      return fieldMeterActor = new FieldMeterActor(body, textureRegion);
    case CurrentCoil:
      return new CurrentCoilActor(body);
    case CurrentSource:
      return new CurrentSourceActor(body, textureRegion);
    case Wire:
      return new CurrentWireActor(body);
    case ElectroMagnet:
      return new ElectromagnetActor(body, textureRegion);
    case Compass:
    default:
      return new Science2DActor(body, textureRegion);
    }
  }
  
  @Override
  public void challenge(boolean challenge) {
    super.challenge(challenge);
    // Enable/Disable compass
    if (compassActor != null) {
      compassActor.setVisible(!challenge);
    }
    if (probeManager == null) {
      probeManager = new ProbeManager(skin, getWidth(), getHeight(), 
          this, controlPanel);        
      new FieldDirectionProber(emModel, probeManager);
      new FieldMagnitudeProber(emModel, probeManager);
      new LightProber(probeManager);
      this.getRoot().addActorBefore(fieldMeterActor, probeManager);     
    }
    if (challenge) {
      probeManager.startChallenge();
    } else {
      probeManager.endChallenge();
    }
  };
  
  public void done(boolean success) {
    if (success) {
      // TODO: put in a proper celebration here
      probeManager.setTitle("Congratulations! You move to the next Level ");
      // ScienceEngine.getPlatformAdapter().showURL("file:///sdcard/data/electromagneticinduction.html");
      challenge(false);
    }
  }
}
