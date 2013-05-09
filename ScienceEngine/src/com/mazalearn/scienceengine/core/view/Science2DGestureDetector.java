package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class Science2DGestureDetector extends GestureDetector {


  public Science2DGestureDetector(Stage science2DView) {
    super(new Science2DGestureAdapter(science2DView));
  }

  public static class Science2DGestureAdapter extends GestureAdapter {
    
    private static final float TOLERANCE = 0.1f;
    private final Stage science2DView;
    private Vector2 p = new Vector2(), center = new Vector2();

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
      // Treat initialPointer1, initialPointer2 as one line and pointer1, pointer2 as second line.
      // then delta is the degree of rotation to rotate the first to the second around their intersection point
      center.set(initialPointer1).sub(initialPointer2);
      p.set(pointer1).sub(pointer2);
      float degrees = center.angle() - p.angle();
      if (degrees > 90 || degrees < -90) {
        degrees = ((degrees - 180) + 360) % 360;
      }
      
      float deltaRadians = degrees * MathUtils.degreesToRadians;
      float radians = body.getAngle() + deltaRadians;
      if (deltaRadians > TOLERANCE || deltaRadians < -TOLERANCE) {
        Gdx.app.error(ScienceEngine.LOG, "Degrees = " + degrees + 
            " rotation = " + body.getAngle() * MathUtils.radiansToDegrees);
        // Let us move the body to the average point of all 4 end points
        float avgX = (initialPointer1.x + initialPointer2.x + pointer1.x + pointer2.x) / 4;
        float avgY = (initialPointer1.y + initialPointer2.y + pointer1.y + pointer2.y) / 4;
        
        science2DView.screenToStageCoordinates(center.set(avgX, avgY)).mul(1f / ScreenComponent.PIXELS_PER_M);
        body.setPositionAndAngle(center, body.getAngle());
        initialPointer2.set(pointer2);
        initialPointer1.set(pointer1);
        rotateConfig.setValue(radians);
        ScienceEngine.selectParameter(body, Parameter.RotationAngle, radians, (IScience2DView) science2DView);
      }
      return true;
    }
  }
}