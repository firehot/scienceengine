package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.OptionsDialog;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.CommandButtonControl;
import com.mazalearn.scienceengine.core.controller.IControl;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.controller.ToggleButtonControl;
import com.mazalearn.scienceengine.core.model.Parameter;

public class ViewControls extends Table implements IControl {
  private static final int VIEW_BUTTON_HEIGHT = 30;
  private static final int VIEW_BUTTON_WIDTH = 110;
  private final IScience2DView science2DView;
  private IControl suspendControl;
  private IControl challengeControl;
  private IControl resetControl;
  private IMessage messages;
  private IScience2DController science2DController;
  private boolean isActivated = false;
  private Actor viewControlPanel;
  
  public ViewControls(IScience2DController science2DController, Skin skin) {
    super(skin);
    this.setName("ViewControls");
    this.science2DController = science2DController;
    this.science2DView = science2DController.getView();
    messages = ScienceEngine.getMsg();
    this.defaults().fill();
    Image image = new Image(new Texture("images/settings.png"));
    image.setSize(VIEW_BUTTON_HEIGHT, VIEW_BUTTON_HEIGHT);
    image.setPosition(VIEW_BUTTON_WIDTH / 2 - VIEW_BUTTON_HEIGHT / 2, 0);
    Button imageButton = new TextButton("", skin, "body");
    imageButton.addActor(image);
    imageButton.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        isActivated = !isActivated();
        viewControlPanel.setVisible(isActivated);
      }
    });
    this.add(imageButton).width(VIEW_BUTTON_WIDTH).height(VIEW_BUTTON_HEIGHT);
    this.row();
    viewControlPanel = createViewControlPanel(skin, science2DView);
    viewControlPanel.setVisible(isActivated());
    this.add(viewControlPanel);
  }
  
  private IMessage getMsg() {
    return messages;
  }
  
  private Actor createViewControlPanel(final Skin skin,
      final IScience2DView science2DView) {
    Table viewControls = new Table(skin);
    viewControls.setName("ViewControls");
    viewControls.defaults()
        .fill()
        .height(VIEW_BUTTON_HEIGHT)
        .width(VIEW_BUTTON_WIDTH)
        .pad(0);
    // Add challenge/learn functionality
    AbstractModelConfig<Boolean> challengeModelConfig = 
        new AbstractModelConfig<Boolean>(null, 
            Parameter.Challenge, false) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DView.challenge(value);}
          public Boolean getValue() { return science2DView.isChallengeInProgress(); }
          public boolean isPossible() { return true; }
    };
    
    challengeControl = new ToggleButtonControl(challengeModelConfig, skin, "body") {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(
            science2DView.isChallengeInProgress() ? 
                getMsg().getString("ViewControls.EndChallenge") : 
                  getMsg().getString("ViewControls.Challenge")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    
    // Add pause/resume functionality for the activity
    AbstractModelConfig<Boolean> pauseResumeModelConfig = 
        new AbstractModelConfig<Boolean>(null, Parameter.PauseResume) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DView.suspend(value); }
          public Boolean getValue() { return science2DView.isSuspended(); }
          public boolean isPossible() { return true; }
    };
    suspendControl = new ToggleButtonControl(pauseResumeModelConfig, skin, "body") {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(science2DView.isSuspended() ? 
            getMsg().getString("ViewControls.Resume") : 
              getMsg().getString("ViewControls.Pause")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };

    // Add reset functionality for the activity
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>(null, Parameter.Reset) { //$NON-NLS-1$ //$NON-NLS-2$
          public void doCommand() { science2DController.reset(); }
          public boolean isPossible() { return true; }
    };
    resetControl = new CommandButtonControl(resetModelConfig, skin, "body");
    
    // Add options dialog for controlling language, music, sound.
    Button optionsButton = new TextButton(
        getMsg().getString("ScienceEngine.Options") + "...", skin, "body");
    optionsButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        new OptionsDialog(getStage(), skin).show(getStage());
      }
    });
    
    viewControls.row();
    viewControls.add(resetControl.getActor());
    viewControls.row();
    viewControls.add(suspendControl.getActor());
    viewControls.row();
    viewControls.add(challengeControl.getActor());
    viewControls.row();
    viewControls.add(optionsButton);
    viewControls.row();
    
    syncWithModel();
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

  public void setAvailable(boolean isActivated) {
    this.isActivated = isActivated;
  }
  
  public boolean isActivated() {
    return isActivated;
  }
}