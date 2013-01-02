package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.CommandButtonControl;
import com.mazalearn.scienceengine.core.controller.IControl;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.controller.ToggleButtonControl;
import com.mazalearn.scienceengine.core.model.Parameter;

public class ViewControls extends Table implements IControl {
  private static final int VIEW_BUTTON_HEIGHT = 30;
  private static final int VIEW_BUTTON_WIDTH = 100;
  private final IScience2DView science2DView;
  private IControl suspendControl;
  private IControl challengeControl;
  private IControl resetControl;
  private IMessage messages;
  private IScience2DController science2DController;
  private boolean isAvailable = true;
  
  public ViewControls(IScience2DController science2DController, Skin skin) {
    super(skin);
    this.setName("ViewControls");
    this.science2DController = science2DController;
    this.science2DView = science2DController.getView();
    messages = ScienceEngine.getMsg();
    this.defaults().fill();
    this.add(createViewControlPanel(skin, science2DView));
  }
  
  private IMessage getMsg() {
    return messages;
  }
  
  private Actor createViewControlPanel(Skin skin,
      final IScience2DView science2DView) {
    Table viewControls = new Table(skin);
    viewControls.setName("ViewControls");
    viewControls.defaults()
        .fill()
        .height(VIEW_BUTTON_HEIGHT)
        .width(VIEW_BUTTON_WIDTH)
        .pad(0, 0, 0, 0);
    // Add challenge/learn functionality
    AbstractModelConfig<Boolean> challengeModelConfig = 
        new AbstractModelConfig<Boolean>(null, 
            Parameter.Challenge, false) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DView.challenge(value);}
          public Boolean getValue() { return science2DView.isChallengeInProgress(); }
          public boolean isPossible() { return true; }
    };
    
    challengeControl = new ToggleButtonControl(challengeModelConfig, skin) {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(
            science2DView.isChallengeInProgress() ? 
                getMsg().getString("ControlPanel.EndChallenge") : 
                  getMsg().getString("ControlPanel.Challenge")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    
    // Add pause/resume functionality for the experiment
    AbstractModelConfig<Boolean> pauseResumeModelConfig = 
        new AbstractModelConfig<Boolean>(null, Parameter.PauseResume) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DView.suspend(value); }
          public Boolean getValue() { return science2DView.isSuspended(); }
          public boolean isPossible() { return true; }
    };
    suspendControl = new ToggleButtonControl(pauseResumeModelConfig, skin) {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(science2DView.isSuspended() ? 
            getMsg().getString("ControlPanel.Resume") : 
              getMsg().getString("ControlPanel.Pause")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };

    // Add reset functionality for the experiment
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>(null, Parameter.Reset) { //$NON-NLS-1$ //$NON-NLS-2$
          public void doCommand() { science2DController.reload(); }
          public boolean isPossible() { return true; }
    };
    resetControl = new CommandButtonControl(resetModelConfig, skin);
    
    viewControls.row();
    viewControls.add(resetControl.getActor());
    viewControls.row();
    viewControls.add(suspendControl.getActor());
    viewControls.row();
    viewControls.add(challengeControl.getActor());
    viewControls.row();
    
    return viewControls;
  }
  
  public void syncWithModel() {
    challengeControl.syncWithModel();
    suspendControl.syncWithModel();
  }
  
  public void enableControls(boolean enable) {
    suspendControl.getActor().setVisible(enable);
    resetControl.getActor().setVisible(enable);
    this.invalidate();
  }

  @Override
  public Actor getActor() {
    return this;
  }

  public void setAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }
  
  public boolean isAvailable() {
    return isAvailable;
  }
}