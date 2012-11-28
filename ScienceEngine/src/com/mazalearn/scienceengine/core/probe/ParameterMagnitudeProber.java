package com.mazalearn.scienceengine.core.probe;

import java.util.Collections;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.DummyBody;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.Science2DActor;

// outcome = function of parameter
// doubts on how parameter change affects magnitude of outcome
// Generate A, B as two parameter points.
// Is the outcome stronger at A or B?
public class ParameterMagnitudeProber extends AbstractScience2DProber {
  
  enum Type {
    Direct,
    Inverse,
    None;
  }
  
  private final class ClickResult extends ClickListener {
    private final IDoneCallback doneCallback;
    private Boolean correct;
    private long touchDownTime;
 
    private ClickResult(IDoneCallback doneCallback) {
      this.doneCallback = doneCallback;
    }
    
    public void setCorrect(Boolean success) {
      this.correct = success;
    }

    @Override
    public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
      super.touchDown(event, x, y, pointer, button);
      touchDownTime = TimeUtils.nanoTime();
      return true;
    }
    
    @Override
    public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
      super.touchUp(event, x, y, pointer, button);
    }
    
    @Override
    public void clicked(InputEvent event, float x, float y) {
      if (correct == null) { // longpress is the correct answer
        long now = TimeUtils.nanoTime();
        boolean isLongPress = (now - touchDownTime) / 1E9f > 1.1f;
        doneCallback.done(isLongPress);
        return;
      }
      doneCallback.done(correct);
    }
  };

  private String[] hints = {
  };
  
  private final Image image1, image2;
  // Temporary vectors
  private float[] points;
  private float[] outcomes;
  private IModelConfig<Float> probeConfig;

  private DummyBody dummy;

  private ClickResult image1Listener;

  private ClickResult image2Listener;

  private Type type;
    
  public ParameterMagnitudeProber(IScience2DModel model, ProbeManager probeManager) {
    super(probeManager);
    image1 = new ProbeImage();
    image1Listener = new ClickResult(probeManager);
    image2Listener = new ClickResult(probeManager);
    image1.addListener(image1Listener);
    image2 = new ProbeImage();
    image2.addListener(image2Listener);
    image1.setX(600 - image1.getWidth()/2);
    image1.setY(175 - image1.getHeight()/2);
    image2.setX(600 - image2.getWidth()/2);
    image2.setY(125 - image2.getWidth()/2);
    this.points = new float[2];
    this.outcomes = new float[2];
    this.addActor(image1);
    this.addActor(image2);
    dummy = (DummyBody) model.findBody(ComponentType.Dummy);
  }
  
  @Override
  public String getTitle() {
    return "Click on ? for the parameter value which will make the motor run faster.\nLong press for equal";
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image1.setVisible(false);
    image2.setVisible(false);
  }
  
  private void generateProbePoints(float[] points) {
    for (int i = 0; i < points.length; i++) {
      points[i] = MathUtils.random(0f, 10f);
    }
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      // Generate two random points P1, P2 in unit circle.
      // If P0.r ~ P1.r AND (P0.x ~ P1.x) OR (P0.y ~ P1.y) try again
      // Scale P0.x, P1.x by magnet width*2 and P0.y, P1.y by magnet height*2
      do {
        generateProbePoints(points);
      } while (!arePointsAcceptable(points, outcomes));
      dummy.setConfigAttribute(probeConfig.getAttribute(), points);
      probeManager.setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
      
      if (type == Type.None) {
        image1Listener.setCorrect(null);
        image2Listener.setCorrect(null);
      } else if (outcomes[0] > outcomes[1]) {
        image1Listener.setCorrect(type == Type.Direct);
        image2Listener.setCorrect(type == Type.Inverse);
      } else {
        image1Listener.setCorrect(type == Type.Inverse);
        image2Listener.setCorrect(type == Type.Direct);
      }
      image2.setVisible(true);
      image1.setVisible(true);
    } else {
      dummy.setConfigAttribute(null, points);
    }
    ScienceEngine.setProbeMode(activate);
    this.setVisible(activate);
  }

  private boolean haveSimilarMagnitudes(float v1, float v2) {
    if (Math.abs(v1 - v2) < ZERO_TOLERANCE) return true;
    if (Math.abs(v1 - v2) / Math.min(v1, v2) < TOLERANCE) return true;
    return false;
  }
  
  private float calculateOutcome(float x) {
    return x;
  }
  
  protected boolean arePointsAcceptable(float[] points, float[] outcomes) {
    outcomes[0] = calculateOutcome(points[0]);
    outcomes[1] = calculateOutcome(points[1]);
    if (haveSimilarMagnitudes(outcomes[0], outcomes[1])) return false;
    return true;
  }

  @Override
  public String[] getHints() {
    return hints;
  }

  public void setProbeConfig(IModelConfig<Float> probeConfig, String type) {
    this.probeConfig = probeConfig;
    this.type = Type.valueOf(type);
  }
}