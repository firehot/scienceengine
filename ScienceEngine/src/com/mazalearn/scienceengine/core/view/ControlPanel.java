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
import com.mazalearn.scienceengine.app.screens.ExperimentHomeScreen;
import com.mazalearn.scienceengine.app.screens.LoadingScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.CommandButtonControl;
import com.mazalearn.scienceengine.core.controller.Controller;
import com.mazalearn.scienceengine.core.controller.IControl;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.controller.OnOffButtonControl;
import com.mazalearn.scienceengine.core.controller.SelectBoxControl;
import com.mazalearn.scienceengine.core.controller.SliderControl;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

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
    modelControlPanel.setName("Model Controls");
    modelControlPanel.defaults().fill();
    registerModelConfigs(modelControlPanel);
    return modelControlPanel;
  }
  
  public void refresh() {
    registerModelConfigs(modelControlPanel);
  }
  
  public List<IModelConfig<?>> getModelConfigs() {
    return science2DModel.getAllConfigs();
  }

  @SuppressWarnings("rawtypes")
  protected void registerModelConfigs(Table modelControlPanel) {
    this.controllers.clear();
    modelControlPanel.clear();
    // Register all model controllers
    for (IModelConfig modelConfig: science2DModel.getAllConfigs()) {
      this.controllers.add(createViewControl(modelConfig, modelControlPanel));
    }
  }

  private Actor createViewControlPanel(Skin skin,
      final IScience2DModel science2DModel,
      final IScience2DStage science2DStage) {
    Table viewControls = new Table(skin);
    viewControls.setName("View Controls");
    viewControls.defaults().fill();
    // Register name
    this.title = new Label(experimentName, skin);
    viewControls.add(title).colspan(2).center();
    viewControls.row();
    // register the back button
    TextButton backButton = new TextButton(getMsg().getString("ControlPanel.Back"), skin); //$NON-NLS-1$
    backButton.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        science2DController.getView().challenge(false);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.getProfileManager().retrieveProfile().setCurrentLevel(0);
        AbstractScreen screen = new ExperimentHomeScreen(ScienceEngine.SCIENCE_ENGINE, experimentName);
        ScienceEngine.SCIENCE_ENGINE.setScreen(
            new LoadingScreen(ScienceEngine.SCIENCE_ENGINE, screen));
      }
    });
    viewControls.add(backButton).height(30).colspan(2);
    viewControls.row();
    
    // Add challenge/learn functionality
    AbstractModelConfig<Boolean> challengeModelConfig = 
        new AbstractModelConfig<Boolean>(getMsg().getString("ControlPanel.Challenge"), 
            "Challenge or Learn", false) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DStage.challenge(value);}
          public Boolean getValue() { return science2DStage.isChallengeInProgress(); }
          public boolean isPossible() { return true; }
    };
    
    challengeControl = new OnOffButtonControl(challengeModelConfig, skin) {
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
        new AbstractModelConfig<Boolean>(getMsg().getString("ControlPanel.PauseResume"), 
            getMsg().getString("ControlPanel.PauseOrResume")) { //$NON-NLS-1$ //$NON-NLS-2$
          public void setValue(Boolean value) { science2DStage.suspend(value); }
          public Boolean getValue() { return science2DStage.isSuspended(); }
          public boolean isPossible() { return true; }
    };
    suspendControl = new OnOffButtonControl(pauseResumeModelConfig, skin) {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(science2DStage.isSuspended() ? 
            getMsg().getString("ControlPanel.Resume") : 
              getMsg().getString("ControlPanel.Pause")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };

    // Add reset functionality for the experiment
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>(getMsg().getString("ControlPanel.Reset"), 
            getMsg().getString("ControlPanel.ResetToInitialState")) { //$NON-NLS-1$ //$NON-NLS-2$
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
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Controller createViewControl(IModelConfig property, Table modelControlTable) {
    Table table = new Table(skin);
    table.setName(property.getName());
    table.defaults().fill().expand();
    IControl control = null;
    switch(property.getType()) {
      case ONOFF: 
        control = new OnOffButtonControl(property, skin);
        table.add(control.getActor());
        //for checkbox - we need - table.add(property.getName()).pad(0, 5, 0, 5);
        break;
      case RANGE: 
        table.add(property.getName());
        table.row();
        control = new SliderControl(property, skin);
        table.add(control.getActor());
        break;
      case LIST:
        control = new SelectBoxControl(property, skin);
        table.add(control.getActor());
        break;
      case COMMAND:
        control = new CommandButtonControl(property, skin);
        table.add(control.getActor());
        break;
    }
    Controller c = new Controller(modelControlTable.add(table), control);
    modelControlTable.row();
    return c;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    challengeControl.syncWithModel();
    suspendControl.syncWithModel();
    for (Controller controller: controllers) {
      controller.validate();
    }
    this.invalidate();
    this.validate();
  }
  
  public void enableControls(boolean enable) {
    suspendResetPanel.setVisible(enable);
    modelControlPanel.setTouchable(enable ? Touchable.enabled : Touchable.disabled);
    this.invalidate();
  }
}