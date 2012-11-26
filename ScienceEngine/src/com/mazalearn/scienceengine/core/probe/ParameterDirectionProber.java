package com.mazalearn.scienceengine.core.probe;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
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
  
  private final class ClickResult extends ClickListener {
    private final IDoneCallback doneCallback;
    private boolean clockwise;
 
    private ClickResult(IDoneCallback doneCallback) {
      this.doneCallback = doneCallback;
    }
    
    public void setClockwise(boolean clockwise) {
      this.clockwise = clockwise;
    }

    @Override
    public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
      super.touchDown(event, x, y, pointer, button);
      spinClockwise.setVisible(true);
      return true;
    }
    
    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      spinClockwise.setVisible(x < image.getWidth() / 2);
      spinAntiClockwise.setVisible(x > image.getWidth() / 2);
    }
    
    @Override
    public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
      super.touchUp(event, x, y, pointer, button);
      boolean success = (clockwise && spinClockwise.isVisible()) ||
          (!clockwise && spinAntiClockwise.isVisible());
      spinClockwise.setVisible(false);
      spinAntiClockwise.setVisible(false);
      doneCallback.done(success);
    }
  };

  private String[] hints = {
      "Use Fleming's left hand rule"
  };
  
  private final Image image, spinClockwise, spinAntiClockwise;
  private ClickResult imageListener;
  private List<IModelConfig<?>> dependConfigs;
    
  public ParameterDirectionProber(IScience2DModel model, ProbeManager probeManager) {
    super(probeManager);
    
    image = new ProbeImage();
    imageListener = new ClickResult(probeManager);
    image.addListener(imageListener);
    image.setX(probeManager.getWidth() / 2 - image.getWidth() / 2 - 50);
    image.setY(probeManager.getHeight() / 2 - image.getHeight() / 2);
    
    spinClockwise = new Image(new TextureRegion(new Texture("images/clockwise.png")));
    spinClockwise.setVisible(false);
    spinClockwise.setSize(spinClockwise.getWidth() * 2, spinClockwise.getHeight() * 2);
    spinAntiClockwise = new Image(new TextureRegion(new Texture("images/anticlockwise.png")));
    spinAntiClockwise.setSize(spinAntiClockwise.getWidth() * 2, spinAntiClockwise.getHeight() * 2);
    spinAntiClockwise.setVisible(false);
    spinClockwise.setX(image.getX() + image.getWidth() / 2 - spinClockwise.getWidth() / 2);
    spinClockwise.setY(image.getY() + image.getHeight() / 2 - spinClockwise.getHeight() / 2);
    spinAntiClockwise.setX(image.getX() + image.getWidth() / 2 - spinAntiClockwise.getWidth() / 2);
    spinAntiClockwise.setY(image.getY() + image.getHeight() / 2 - spinAntiClockwise.getHeight() / 2);

    this.addActor(image);
    this.addActor(spinClockwise);
    this.addActor(spinAntiClockwise);
  }
  
  @Override
  public String getTitle() {
    return "Click on ? for the parameter value and indicate direction of rotation of motor.";
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image.setVisible(false);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      probeManager.setupProbeConfigs(dependConfigs, false);
      boolean current = (Boolean) dependConfigs.get(0).getValue();
      boolean magnet =  (Boolean) dependConfigs.get(1).getValue();
      imageListener.setClockwise(current == magnet);
      image.setVisible(true);
    } 
    ScienceEngine.setProbeMode(activate);
    ScienceEngine.setSelectedBody(null);
    this.setVisible(activate);
  }
  
  @Override
  public String[] getHints() {
    return hints;
  }

  public void setProbeConfig(List<IModelConfig<?>> dependConfigs, String type) {
    this.dependConfigs = dependConfigs;
  }
}