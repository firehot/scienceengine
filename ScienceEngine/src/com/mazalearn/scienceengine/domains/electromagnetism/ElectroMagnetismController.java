package com.mazalearn.scienceengine.domains.electromagnetism;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.domains.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldDirectionProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.FieldMagnitudeProber;
import com.mazalearn.scienceengine.domains.electromagnetism.probe.LightProber;
import com.mazalearn.scienceengine.domains.electromagnetism.view.AmmeterActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.BarMagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentCoilActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CurrentSourceActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.DrawingActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.DynamoActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.ElectromagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.FieldMeterActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.LightbulbActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.MagnetActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.PickupCoilActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.ScienceTrainActor;
import com.mazalearn.scienceengine.domains.electromagnetism.view.WireActor;
import com.mazalearn.scienceengine.guru.AbstractTutor;
import com.mazalearn.scienceengine.guru.Abstractor;
import com.mazalearn.scienceengine.guru.ITutor;

/**
 * Electromagnetism Domain
 */
public class ElectroMagnetismController extends AbstractScience2DController {
  
  public static final String DOMAIN = "Electromagnetism";

  public ElectroMagnetismController(int level, int width, int height, Skin skin) {
    super(DOMAIN, level, skin);
    AbstractScience2DModel emModel = new ElectroMagnetismModel();
    AbstractScience2DView emView = 
        new ElectroMagnetismView(width, height, emModel, skin, this);
    this.initialize(emModel,  emView);
  }

  @Override
  protected Actor createActor(String type, String viewSpec, Science2DBody body) {
    ComponentType componentType;
    try {
      componentType = ComponentType.valueOf(type);
    } catch(IllegalArgumentException e) {
      return super.createActor(type, viewSpec, body);
    }
    
    String textureFilename = componentType.getTextureFilename();
    if (textureFilename == null) return null;

    TextureRegion textureRegion = 
        new TextureRegion(ScienceEngine.assetManager.get(textureFilename, Texture.class));
    
    switch (componentType) {
    case Ammeter:
      return new AmmeterActor(body, textureRegion);
    case BarMagnet:
      return new BarMagnetActor(body, textureRegion, science2DView.getFont());
    case Lightbulb:
      return new LightbulbActor((Lightbulb) body, textureRegion);
    case PickupCoil:
      return new PickupCoilActor((PickupCoil) body, textureRegion);
    case FieldMeter:
      return new FieldMeterActor(body, textureRegion);
    case CurrentCoil:
      return new CurrentCoilActor(body, science2DView.getFont());
    case CurrentSource:
      return new CurrentSourceActor(body, textureRegion);
    case Wire:
      return new WireActor(body);
    case ElectroMagnet:
      return new ElectromagnetActor(body, textureRegion);
    case Drawing:
      return new DrawingActor(body, textureRegion, viewSpec, science2DView.getFont(), skin);
    case Dynamo:
      return new DynamoActor(body, textureRegion);
    case Magnet:
      return new MagnetActor(body, textureRegion);
    case ScienceTrain:
      return new ScienceTrainActor(body, science2DView, skin);
    case Compass:
    default:
      return new Science2DActor(body, textureRegion);
    }
  }

  @Override
  public AbstractTutor createTutor(ITutor parent, String type, String goal, String name,
      Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    if ("FieldMagnitudeProber".equals(type)) {
      return new FieldMagnitudeProber(this, parent, goal, name, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    } else if ("FieldDirectionProber".equals(type)) {
      return new FieldDirectionProber(this, parent, goal, name, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    } else if ("LightProber".equals(type)) {
      return new LightProber(this, parent, goal, name, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    }
    return super.createTutor(parent, type, goal, name, components, configs, deltaSuccessScore, deltaFailureScore, hints);
  }
}
