package com.mazalearn.scienceengine.domains.electromagnetism;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.domains.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldDirectionProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldMagnitudeProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.LightProber;
import com.mazalearn.scienceengine.domains.electromagnetism.view.AmmeterActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.BarMagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CircuitActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentCoilActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentSourceActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.DrawingActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.ElectromagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.FieldMeterActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.PickupCoilActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.ScienceTrain;
import com.mazalearn.scienceengine.domains.electromagnetism.view.WireActor;
import com.mazalearn.scienceengine.guru.AbstractTutor;
import com.mazalearn.scienceengine.guru.Abstractor;

public class ElectroMagnetismView extends AbstractScience2DView {
  private FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();

  public ElectroMagnetismView(float width, float height,
      final AbstractScience2DModel emModel, Skin skin, IScience2DController controller) {
    super(emModel, width, height, skin, controller);
    
    getRoot().addListener(new ClickListener() {   
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        if (event.getTarget() == getRoot()) {
          ScienceEngine.selectBody(fieldMeter, ElectroMagnetismView.this);
          if (fieldMeter != null && fieldMeter.isActive() && event.getTarget() == getRoot()) {
            // Move field sampler here and convert to model coords
            pos.set(x, y).mul(1f / ScienceEngine.PIXELS_PER_M);
            fieldMeter.setPositionAndAngle(pos, 0);
          }
        }
      }
    });
  }

  @Override
  public void prepareView() {
    // TODO: prepareActor should be called for all actors
    // TODO: FieldMeter should manage its own clicks - make it larger and in the background above root
    fieldMeter = (FieldMeter) science2DModel.findBody(ComponentType.FieldMeter);
    for (List<CircuitElement> circuit: science2DModel.getCircuits()) {
      this.addActor(new CircuitActor(circuit));
    }
    super.prepareView();
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
    case Ammeter:
      return new AmmeterActor(body, textureRegion);
    case BarMagnet:
      return new BarMagnetActor(body, textureRegion, getFont());
    case Lightbulb:
      return new LightbulbActor((Lightbulb) body, textureRegion);
    case PickupCoil:
      return new PickupCoilActor((PickupCoil) body, textureRegion);
    case FieldMeter:
      return new FieldMeterActor(body, textureRegion);
    case CurrentCoil:
      return new CurrentCoilActor(body, getFont());
    case CurrentSource:
      return new CurrentSourceActor(body, textureRegion);
    case Wire:
      return new WireActor(body);
    case ElectroMagnet:
      return new ElectromagnetActor(body, textureRegion);
    case Drawing:
      return new DrawingActor(body, textureRegion, getFont(), skin);
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
    if (type.equals("Train")) {
      return new ScienceTrain(this);
    }
    return null;
  }

  @Override
  public AbstractTutor createTutor(String type, String goal, 
      Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore) {
    if ("FieldMagnitudeProber".equals(type)) {
      return new FieldMagnitudeProber(science2DModel, this, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    } else if ("FieldDirectionProber".equals(type)) {
      return new FieldDirectionProber(science2DModel, this, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    } else if ("LightProber".equals(type)) {
      return new LightProber(science2DModel, this, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    } else if ("Abstractor".equals(type)) {
      return new Abstractor(science2DModel, this, goal, components, configs, skin, 
          controlPanel, deltaSuccessScore, deltaFailureScore);
    }
    return super.createTutor(type, goal, components, configs, deltaSuccessScore, deltaFailureScore);
  }
}
