package com.mazalearn.scienceengine.domains.electromagnetism;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Dynamo;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet;
import com.mazalearn.scienceengine.domains.electromagnetism.view.CircuitActor;

public class ElectroMagnetismView extends AbstractScience2DView {
  private FieldMeter fieldMeter;
  private Dynamo dynamo;
  private Magnet magnet;
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
  public void act(float delta) {
    super.act(delta);
    if (dynamo != null && magnet != null) {
      // Clearance of 1 unit between magnet and dynamo is enforced
      magnet.setMaxWidth(dynamo.getWidth() - 1f);
      dynamo.setMinWidth(magnet.getWidth() + 1f);
      float strength = magnet.getStrength();
      float scale = (float) Math.pow(dynamo.getWidth() - magnet.getWidth(), 2);
      dynamo.setMagnetFlux(strength / scale);
    }
  }
  
  @Override
  public void prepareView() {
    // TODO: prepareActor should be called for all actors
    // TODO: FieldMeter should manage its own clicks - make it larger and in the background above root
    fieldMeter = (FieldMeter) science2DModel.findBody(ComponentType.FieldMeter);
    dynamo = (Dynamo) science2DModel.findBody(ComponentType.Dynamo);
    magnet = (Magnet) science2DModel.findBody(ComponentType.Magnet);
    for (List<CircuitElement> circuit: science2DModel.getCircuits()) {
      this.addActor(new CircuitActor(circuit));
    }
    super.prepareView();
  }
}
