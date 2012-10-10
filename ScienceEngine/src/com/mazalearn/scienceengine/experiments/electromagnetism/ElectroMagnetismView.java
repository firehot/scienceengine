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
    
    Actor coilsBack = new Image(new Texture("images/coppercoils-back.png"));
    coilsBack.setName("CoilsBack");
    Actor brushes = new Image(new Texture("images/brush.png"));
    brushes.setName("Brushes");
    this.addActor(coilsBack);
    this.addActor(brushes);
    for (final Science2DBody body: emModel.getBodies()) {
      ComponentType componentType = ComponentType.valueOf(body.getComponentType());
      String textureFilename = componentType.getTextureFilename();
      if (textureFilename == null) continue;

      TextureRegion textureRegion = 
          new TextureRegion(new Texture(textureFilename));
      switch (componentType) {
      case BarMagnet:
        barMagnetActor = new BarMagnetActor(textureRegion, body, this, emModel);
        this.addActor(barMagnetActor);
        break;
      case Lightbulb:
        this.addActor(new LightbulbActor(textureRegion, (Lightbulb) body));
        break;
      case PickupCoil:
        this.addActor(new PickupCoilActor((PickupCoil) body, textureRegion));
        break;
      case FieldMeter:
        this.addActor(fieldMeterActor = new FieldMeterActor(textureRegion, body));
        fieldMeter = (FieldMeter) body;
        break;
      case CurrentCoil:
        this.addActor(new CurrentCoilActor(body));
        break;        
      case CurrentSource:
        this.addActor(new CurrentSourceActor(body, textureRegion));
        break;        
      case CurrentWire:
        this.addActor(new CurrentWireActor(body));
        break;
      case ElectroMagnet:
        this.addActor(new ElectromagnetActor(body, textureRegion));
        break;
      case Compass:
        this.addActor(compassActor = new Science2DActor(body, textureRegion));
        break;
      default:
        this.addActor(new Science2DActor(body, textureRegion));
        break;
      }
    }
    this.addActor(new CircuitActor(emModel));
    
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
  
  @Override
  public void challenge(boolean challenge) {
    super.challenge(challenge);
    // Enable/Disable field meter, compass
    compassActor.setVisible(!challenge);
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
      int level = getLevelManager().getLevel() + 1;
      // TODO: put in a proper celebration here
      probeManager.setTitle("Congratulations! You move to Level " + level);
      challenge(false);
      getLevelManager().setLevel(level);
      getLevelManager().load();
    }
  }
}
