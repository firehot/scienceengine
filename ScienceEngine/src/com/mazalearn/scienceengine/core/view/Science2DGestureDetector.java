package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class Science2DGestureDetector extends GestureDetector {


  public Science2DGestureDetector(Stage science2DView) {
    super(new Science2DGestureAdapter(science2DView));
  }

  public static class Science2DGestureAdapter extends GestureAdapter {
    
    private final Stage science2DView;
    private Vector2 p = new Vector2();

    public Science2DGestureAdapter(Stage science2DView) {
      super();
      this.science2DView = science2DView;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
        Vector2 pointer1, Vector2 pointer2) {
      science2DView.screenToStageCoordinates(p.set(initialPointer1));
      Actor a1 = science2DView.hit(p.x, p.y, true);
      science2DView.screenToStageCoordinates(p.set(pointer1));
      Actor a2 = science2DView.hit(p.x, p.y, true);
      if (a2 != a1 || !(a1 instanceof Science2DActor)) return false;
      Science2DActor science2DActor = (Science2DActor) a1;
      Science2DBody body = science2DActor.getBody();
      IModelConfig<Float> rotateConfig = null;
      for (IModelConfig<?> config: body.getConfigs()) {
        if (config.getParameter() == Parameter.RotationAngle) {
          rotateConfig = (IModelConfig<Float>) config;
        }
      }
      if (rotateConfig == null || !rotateConfig.isAvailable()) return false;
      // Treat initialPointer2 and pointer2 as position vectors from pointer1 - the delta is the degree of rotation
      p.set(pointer2).sub(pointer1);
      float degrees = p.angle();
      p.set(initialPointer2).sub(pointer1);
      initialPointer2.set(pointer2);
      initialPointer1.set(pointer1);
      degrees -= p.angle();
      float radians = body.getAngle() - degrees * MathUtils.degreesToRadians;
      rotateConfig.setValue(radians);
      ScienceEngine.selectParameter(body, Parameter.RotationAngle, radians, (IScience2DView) science2DView);
      return true;
    }
  }
}