package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.screens.DomainHomeScreen;
import com.mazalearn.scienceengine.app.screens.LoadingScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.CommandButtonControl;
import com.mazalearn.scienceengine.core.controller.Controller;
import com.mazalearn.scienceengine.core.controller.IControl;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.controller.ToggleButtonControl;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class ControlPanel extends Table {
  private final IScience2DController science2DController;
  private final IScience2DModel science2DModel;
  private final IScience2DStage science2DStage;
  private final Skin skin;
  private List<Controller> controllers = new ArrayList<Controller>();
  private String experimentName;
  private IControl suspendControl;
  private IControl challengeControl;
  private Table modelControlPanel;
  private Label title;
  private Table suspendResetPanel; // part of viewcontrolpanel
  private IMessage messages;
  
  public ControlPanel(Skin skin, IScience2DController science2DController) {
    super(skin);
    this.skin = skin;
    this.setName("ControlPanel");
    this.science2DController = science2DController;
    this.science2DModel = science2DController.getModel();
    this.science2DStage = science2DController.getView();
    this.experimentName = science2DController.getName();
    messages = ScienceEngine.getMsg();
    this.defaults().fill();
    Actor viewControlPanel = 
        createViewControlPanel(skin, science2DModel, science2DStage);
    this.modelControlPanel = createModelControlPanel(skin);
    this.add(viewControlPanel);
    this.row();
    this.add(modelControlPanel);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
  }
  
  private IMessage getMsg() {
    return messages;
  }

  public Table createModelControlPanel(Skin skin) {
    Table modelControlPanel = new Table(skin);
    modelControlPanel.setName("ModelControls");
    modelControlPanel.defaults().fill();
    registerModelConfigs(modelControlPanel);
    return modelControlPanel;
  }
  
  public void refresh() {
    registerModelConfigs(modelControlPanel);
  }
  
  @SuppressWarnings("rawtypes")
  protected void registerModelConfigs(Table modelControlPanel) {
    this.controllers.clear();
    modelControlPanel.clear();
    // Register Environment into ControlPanel
    Science2DActor environment = 
        (Science2DActor) science2DStage.findActor(ComponentType.Environment.name());
    if (environment != null) {
      modelControlPanel.add(environment);
      modelControlPanel.row();
    }    
    AbstractModelConfig<String> selectedBodyConfig = 
        new AbstractModelConfig<String>(null,
            Parameter.NameOfSelectedBody, "") { //$NON-NLS-1$ //$NON-NLS-2$
          public String getValue() { 
            Science2DBody body = ScienceEngine.getSelectedBody();
            return body != null ? body.getComponentType().toString() : "";
          }
          public boolean isPossible() { return true; }
          public boolean isAvailable() { return true; }
    };
    
    this.controllers.add(Controller.createController(selectedBodyConfig, modelControlPanel, skin));
    modelControlPanel.row();
    // Register all model controllers
    for (IModelConfig modelConfig: science2DModel.getAllConfigs()) {
      this.controllers.add(Controller.createController(modelConfig, modelControlPanel, skin));
    }
  }

  private Actor createViewControlPanel(Skin skin,
      final IScience2DModel science2DModel,
      final IScience2DStage science2DStage) {
    Table viewControls = new Table(skin);
    viewControls.setName("ViewControls");
    viewControls.defaults().fill();
    // Register name
    this.title = new Label(experimentName, skin);
    viewControls.add(title).colspan(2).center();
    viewControls.row();
    // register the back button
    final TextButton backButton = new TextButton(getMsg().getString("ControlPanel.Back"), skin); //$NON-NLS-1$
    backButton.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        science2DController.getView().challenge(false);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.getProfileManager().retrieveProfile().setCurrentLevel(0);
        AbstractScreen screen = new DomainHomeScreen(ScienceEngine.SCIENCE_ENGINE, experimentName);
        ScienceEngine.SCIENCE_ENGINE.setScreen(
            new LoadingScreen(ScienceEngine.SCIENCE_ENGINE, screen));
      }
      
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        super.touchDown(event, localX, localY, pointer, button);
        IScience2DStage stage = (IScience2DStage) backButton.getStage();
        Label status = (Label) stage.findActor(StageComponent.Status.name());
        status.setText(ScienceEngine.getMsg().getString("Help.Back"));
        return true;
      }
    });
    viewControls.add(backButton).height(30).colspan(2);
    viewControls.row();
    
    // Add challenge/learn functionality
    AbstractModelConfig<Boolean> challengeModelConfig = 
        new AbstractModelConfig<Boolean>(null, 
            Parameter.Challenge, false) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DStage.challenge(value);}
          public Boolean getValue() { return science2DStage.isChallengeInProgress(); }
          public boolean isPossible() { return true; }
    };
    
    challengeControl = new ToggleButtonControl(challengeModelConfig, skin) {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(
            science2DStage.isChallengeInProgress() ? 
                getMsg().getString("ControlPanel.EndChallenge") : 
                  getMsg().getString("ControlPanel.Challenge")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    viewControls.add(challengeControl.getActor()).height(30).colspan(2);
    viewControls.row();
    
    // Add pause/resume functionality for the experiment
    AbstractModelConfig<Boolean> pauseResumeModelConfig = 
        new AbstractModelConfig<Boolean>(null, Parameter.PauseResume) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DStage.suspend(value); }
          public Boolean getValue() { return science2DStage.isSuspended(); }
          public boolean isPossible() { return true; }
    };
    suspendControl = new ToggleButtonControl(pauseResumeModelConfig, skin) {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(science2DStage.isSuspended() ? 
            getMsg().getString("ControlPanel.Resume") : 
              getMsg().getString("ControlPanel.Pause")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };

    // Add reset functionality for the experiment
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>(null, Parameter.Reset) { //$NON-NLS-1$ //$NON-NLS-2$
          public void doCommand() { science2DModel.reset(); }
          public boolean isPossible() { return true; }
    };
    IControl resetControl = new CommandButtonControl(resetModelConfig, skin);
    
    suspendResetPanel = new Table(skin);
    suspendResetPanel.setName("SuspendReset");
    suspendResetPanel.defaults().fill().expand();
    suspendResetPanel.add(suspendControl.getActor()).pad(0,5,0, 5);
    suspendResetPanel.add(resetControl.getActor());
    
    viewControls.add(suspendResetPanel).pad(0, 0, 10, 0);
    viewControls.row();
    return viewControls;
  }
  
  public Label getTitle() {
    return this.title;
  }
  
  @Override
  public void act(float delta) {
    syncWithModel();
    super.act(delta);
    this.invalidate();
    this.validate();
  }

  public void syncWithModel() {
    challengeControl.syncWithModel();
    suspendControl.syncWithModel();
    for (Controller controller: controllers) {
       controller.validate();
     }
  }
  
  public void enableControls(boolean enable) {
    suspendResetPanel.setVisible(enable);
    modelControlPanel.setTouchable(enable ? Touchable.enabled : Touchable.disabled);
    this.invalidate();
  }
}