package com.mazalearn.scienceengine.domains.electromagnetism;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.probe.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.core.view.StageComponent;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.domains.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldDirectionProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldMagnitudeProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.LightProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.VariablesProber;
import com.mazalearn.scienceengine.domains.electromagnetism.view.BarMagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CircuitActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentCoilActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentSourceActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.WireActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.ElectromagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.FieldMeterActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.PickupCoilActor;

public class ElectroMagnetismView extends AbstractScience2DStage {
  private FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
  private ElectroMagnetismModel emModel;
  private Actor compassActor;
  private IScience2DController controller;

  public ElectroMagnetismView(float width, float height,
      final ElectroMagnetismModel emModel, Skin skin, IScience2DController controller) {
    super(emModel, width, height, skin);
    this.emModel = emModel;
    this.controller = controller;
    
    getRoot().addListener(new ClickListener() {
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        if (event.getTarget() == getRoot()) {
          ScienceEngine.setSelectedBody(null);
          if (fieldMeter != null && fieldMeter.isActive() && event.getTarget() == getRoot()) {
            // Move field sampler here and convert to model coords
            pos.set(x, y).mul(1f / ScienceEngine.PIXELS_PER_M);
            fieldMeter.setPositionAndAngle(pos, 0);
            Label status = (Label) findActor(StageComponent.Status.name());
            status.setText(
                ScienceEngine.getMsg().getString("Name." + fieldMeter.getComponentType().name()) + 
                "  -  " +
                ScienceEngine.getMsg().getString("Help." + fieldMeter.getComponentType().name()));
          }
        }
      }       
    });
  }

  @Override
  public void prepareStage() {
    fieldMeter = (FieldMeter) emModel.findBody(ComponentType.FieldMeter);
    compassActor = findActor(ComponentType.Compass.name());
    for (List<CircuitElement> circuit: emModel.getCircuits()) {
      this.addActor(new CircuitActor(circuit));
    }
    super.prepareStage();
  }
  
  @Override
  protected Actor createActor(Science2DBody body) {
    ComponentType componentType = ComponentType.valueOf(body.getComponentType());
    
    if (componentType == null) {
      return super.createActor(body);
    }
    
    String textureFilename = componentType.getTextureFilename();
    if (textureFilename == null) return null;

    TextureRegion textureRegion = 
        new TextureRegion(ScienceEngine.assetManager.get(textureFilename, Texture.class));
    
    switch (componentType) {
    case BarMagnet:
      return new BarMagnetActor(body, textureRegion, getFont());
    case Lightbulb:
      return new LightbulbActor((Lightbulb) body, textureRegion);
    case PickupCoil:
      return new PickupCoilActor((PickupCoil) body, textureRegion);
    case FieldMeter:
      return new FieldMeterActor(body, textureRegion);
    case CurrentCoil:
      return new CurrentCoilActor(body);
    case CurrentSource:
      return new CurrentSourceActor(body, textureRegion);
    case Wire:
      return new WireActor(body);
    case ElectroMagnet:
      return new ElectromagnetActor(body, textureRegion);
    case Compass:
    default:
      return new Science2DActor(body, textureRegion);
    }
  }
  
  @Override
  protected Actor createActor(String type) {
    if (type.equals("CoilsBack")) {
      return new Image(ScienceEngine.assetManager.get("images/coppercoils-back.png", Texture.class));
    }
    if (type.equals("Brushes")) {
      return new Image(ScienceEngine.assetManager.get("images/brush.png", Texture.class));
    }
    return null;
  }

    @Override
  public void challenge(boolean challenge) {
    super.challenge(challenge);
    // Enable/Disable compass
    if (compassActor != null) {
      compassActor.setVisible(!challenge);
    }
    if (challenge) {
      getProbeManager().startChallenge();
    } else {
      getProbeManager().endChallenge();
    }
  };
  
  @Override
  public AbstractScience2DProber createProber(String name, ProbeManager probeManager) {
    if ("FieldMagnitudeProber".equals(name)) {
      return new FieldMagnitudeProber(emModel, probeManager);
    } else if ("FieldDirectionProber".equals(name)) {
      return new FieldDirectionProber(emModel, probeManager);
    } else if ("LightProber".equals(name)) {
      return new LightProber(emModel, probeManager);
    } else if ("VariablesProber".equals(name)) {
      return new VariablesProber(probeManager, emModel, skin, findActor("ModelControls"), controlPanel);
    }
    return super.createProber(name, probeManager);
  }
  
  public void done(boolean success) {
    if (success) {
      // TODO: put in a proper celebration here
      getProbeManager().setTitle("Congratulations! You move to the next Level ");
      // TODO: generalize
      ScienceEngine.getPlatformAdapter().showURL(
          "data/" + controller.getName() + "/" + controller.getLevel() + ".html");
      challenge(false);
    } else {
      super.challenge(false);
    }
  }
}
