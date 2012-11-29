package com.mazalearn.scienceengine.core.probe;

import java.util.Collections;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.DummyBody;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

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
  
  private String[] hints = {
  };
  
  private final Image image;
  private final Image decrease, increase, dontCare;
  private IModelConfig<Float> probeConfig;

  private DummyBody dummy;

  private ClickResult imageListener;

  private Type type;
    
  public ParameterMagnitudeProber(IScience2DModel model, ProbeManager probeManager) {
    super(probeManager);
    image = new ProbeImage();
    image.setX(700 - image.getWidth()/2);
    image.setY(175 - image.getHeight()/2);
   
    decrease = createResultImage("images/fieldarrow.png", 2);
    decrease.setPosition(image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 3);
    decrease.setRotation(180);
    dontCare = createResultImage("images/cross.png", 1);
    dontCare.setPosition(image.getX() + image.getWidth() / 2 - dontCare.getWidth()/2, 
        image.getY() + image.getHeight() / 2 - dontCare.getHeight()/2);
    increase = createResultImage("images/fieldarrow.png", 2);
    increase.setPosition(image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 3);

    imageListener = new ClickResult(probeManager, new Image[] {decrease, increase, dontCare},
        new ClickResult.StateMapper() {
      @Override
      public int map(float x, float y) {
        if (x < image.getWidth() / 2 && y > image.getHeight() / 2) return 0;
        if (x > image.getWidth() / 2 && y > image.getHeight() / 2) return 1;
        return 2;
      }
    });
    image.addListener(imageListener);   

    this.addActor(image);
    this.addActor(decrease);
    this.addActor(increase);
    this.addActor(dontCare);
    dummy = (DummyBody) model.findBody(ComponentType.Dummy);
  }
  
  private Image createResultImage(String path, float scale) {
    Image image = new Image(new Texture(path));
    image.setVisible(false);
    image.setSize(image.getWidth() * scale, image.getHeight() * scale);
    image.setOrigin(0, image.getHeight() / 2);
    return image;
  }
    
  @Override
  public String getTitle() {
    return "Select decrease or increase parameter to make the motor run faster.\n" +
    		"Select X if parameter does not affect motor.";
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image.setVisible(false);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      float value = MathUtils.random(0f, 10f);
      dummy.setConfigAttribute(probeConfig.getAttribute(), value);
      probeManager.setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
      
      switch (type) {
      case None: imageListener.setResult(dontCare); break;
      case Direct: imageListener.setResult(increase); break;
      case Inverse: imageListener.setResult(decrease); break;
      }
      image.setVisible(true);
    } else {
      dummy.setConfigAttribute(null, 0);
    }
    ScienceEngine.setProbeMode(activate);
    this.setVisible(activate);
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