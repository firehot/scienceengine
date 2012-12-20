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
import com.mazalearn.scienceengine.domains.electromagnetism.probe.VariablesProber;
import com.mazalearn.scienceengine.domains.electromagnetism.view.AmmeterActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.BarMagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CircuitActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentCoilActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentSourceActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.ElectromagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.FieldMeterActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.PickupCoilActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.WireActor;
import com.mazalearn.scienceengine.guru.AbstractTutor;

public class ElectroMagnetismView extends AbstractScience2DView {
  private FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
  private AbstractScience2DModel emModel;

  public ElectroMagnetismView(float width, float height,
      final AbstractScience2DModel emModel, Skin skin, IScience2DController controller) {
    super(emModel, width, height, skin, controller);
    this.emModel = emModel;
    
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
    fieldMeter = (FieldMeter) emModel.findBody(ComponentType.FieldMeter);
    for (List<CircuitElement> circuit: emModel.getCircuits()) {
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
    // Enable/Disable compass
//    if (compassActor != null) {
//      compassActor.setVisible(!challenge);
//    }
    super.challenge(challenge);
  };
  
  @Override
  public AbstractTutor createTutor(String name, String type, 
      int deltaSuccessScore, int deltaFailureScore) {
    if ("FieldMagnitudeProber".equals(name)) {
      return new FieldMagnitudeProber(emModel, this, deltaSuccessScore, deltaFailureScore);
    } else if ("FieldDirectionProber".equals(name)) {
      return new FieldDirectionProber(emModel, this, deltaSuccessScore, deltaFailureScore);
    } else if ("LightProber".equals(name)) {
      return new LightProber(emModel, this, deltaSuccessScore, deltaFailureScore);
    } else if ("VariablesProber".equals(name)) {
      return new VariablesProber(emModel, this, skin, findActor("ModelControls"), controlPanel, deltaSuccessScore, deltaFailureScore);
    }
    return super.createTutor(name, type, deltaSuccessScore, deltaFailureScore);
  }
}
