package com.mazalearn.scienceengine.core.probe;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelLoader;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

// outcome = function of parameter
// doubts on how parameter change affects magnitude of outcome
// Generate A, B as two parameter points.
// Is the outcome stronger at A or B?
public class ParameterDirectionProber extends AbstractScience2DProber {
  
  enum Type {
    Spin,
    None;
  }
  
  private String[] hints = {
      "Use Fleming's left hand rule"
  };
  
  private final Image image;
  private final Image clockwise, antiClockwise, dontCare;
  private ClickResult imageListener;
  private List<IModelConfig<?>> dependConfigs;

  private Array<?> configs;

  private IScience2DModel science2DModel;
  
  private Image createResultImage(String path, float x, float y) {
    Image image = new Image(new Texture(path));
    image.setVisible(false);
    image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
    return image;
  }
    
  public ParameterDirectionProber(IScience2DModel science2DModel, ProbeManager probeManager) {
    super(probeManager);
    this.science2DModel = science2DModel;
    
    image = new ProbeImage();
    image.setX(probeManager.getWidth() / 2 - image.getWidth() / 2 - 50);
    image.setY(probeManager.getHeight() / 2 - image.getHeight() / 2);
    
    clockwise = createResultImage("images/clockwise.png", 
        image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
    dontCare = createResultImage("images/cross.png", 
        image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
    antiClockwise = createResultImage("images/anticlockwise.png", 
        image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);

    imageListener = new ClickResult(probeManager, new Image[] {clockwise, antiClockwise, dontCare},
        new ClickResult.StateMapper() {
      @Override
      public int map(float x, float y) {
        if (x < image.getWidth() / 2 && y > 0) return 0;
        if (x > image.getWidth() / 2&& y > 0) return 1;
        return 2;
      }
    });
    image.addListener(imageListener);   

    this.addActor(image);
    this.addActor(clockwise);
    this.addActor(antiClockwise);
    this.addActor(dontCare);
  }
  
  @Override
  public String getTitle() {
    return "Click on ? for the parameter value and indicate direction of rotation of motor.";
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image.setVisible(false);
    LevelLoader.readConfigs(configs, science2DModel);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      probeManager.setupProbeConfigs(dependConfigs, false);
      boolean current = (Boolean) dependConfigs.get(0).getValue();
      boolean magnet =  (Boolean) dependConfigs.get(1).getValue();
      imageListener.setResult(current == magnet ? clockwise : antiClockwise);
      image.setVisible(true);
    } 
    ScienceEngine.setProbeMode(activate);
    ScienceEngine.selectBody(null, null);
    this.setVisible(activate);
  }
  
  @Override
  public String[] getHints() {
    return hints;
  }

  public void setProbeConfig(List<IModelConfig<?>> dependConfigs, String type, Array<?> configs) {
    this.dependConfigs = dependConfigs;
    this.configs = configs;
  }
}