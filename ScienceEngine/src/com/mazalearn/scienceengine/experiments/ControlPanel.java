package com.mazalearn.scienceengine.experiments;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.screens.ExperimentHomeScreen;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.Controller;
import com.mazalearn.scienceengine.core.controller.CommandButtonControl;
import com.mazalearn.scienceengine.core.controller.OnOffButtonControl;
import com.mazalearn.scienceengine.core.controller.SelectBoxControl;
import com.mazalearn.scienceengine.core.controller.SliderControl;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IControl;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DStage;

public class ControlPanel extends Table {
  private final IScience2DController science2DController;
  private final IScience2DModel science2DModel;
  private final IScience2DStage science2DStage;
  private final Skin skin;
  private List<Controller> controllers;
  private String experimentName;
  private IControl suspendControl;
  private IControl challengeControl;
  private Table modelControlPanel;
  private Label title;
  private Table suspendResetPanel; // part of viewcontrolpanel
  
  public ControlPanel(Skin skin, IScience2DController science2DController) {
    super(skin, null, science2DController.getName());
    this.skin = skin;
    this.science2DController = science2DController;
    this.science2DModel = science2DController.getModel();
    this.science2DStage = science2DController.getView();
    this.experimentName = science2DController.getName();
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

  public Table createModelControlPanel(Skin skin) {
    Table modelControlPanel = new Table(skin);
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
    this.controllers = new ArrayList<Controller>();
    modelControlPanel.clear();
    // Register all model controllers
    for (IModelConfig modelConfig: science2DModel.getAllConfigs()) {
      this.controllers.add(createViewControl(modelConfig, modelControlPanel));
    }
  }

  protected Actor createViewControlPanel(Skin skin,
      final IScience2DModel science2DModel,
      final IScience2DStage science2DStage) {
    Table viewControls = new Table(skin, null, "Standard");
    viewControls.defaults().fill();
    // Register name
    this.title = new Label(experimentName, skin);
    viewControls.add(title).colspan(2).center();
    viewControls.row();
    // register the back button
    TextButton backButton = new TextButton("Back", skin);
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        science2DController.getView().challenge(false);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.SCIENCE_ENGINE.setScreen(
            new ExperimentHomeScreen(ScienceEngine.SCIENCE_ENGINE, science2DController));
      }
    });
    viewControls.add(backButton).height(30).colspan(2);
    viewControls.row();
    
    // Add challenge/learn functionality
    AbstractModelConfig<Boolean> challengeModelConfig = 
        new AbstractModelConfig<Boolean>("Challenge", "Challenge or Learn", false) {
          public void setValue(Boolean value) { science2DStage.challenge(value);}
          public Boolean getValue() { return science2DStage.isChallengeInProgress(); }
          public boolean isPossible() { return true; }
    };
    
    challengeControl = new OnOffButtonControl(challengeModelConfig, skin) {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(
            science2DStage.isChallengeInProgress() ? "End Challenge" : "Challenge");
      }
    };
    viewControls.add(challengeControl.getActor()).height(30).colspan(2);
    viewControls.row();
    
    // Add pause/resume functionality for the experiment
    AbstractModelConfig<Boolean> pauseResumeModelConfig = 
        new AbstractModelConfig<Boolean>("Pause/Resume", "Pause or Resume") {
          public void setValue(Boolean value) { science2DStage.suspend(value); }
          public Boolean getValue() { return science2DStage.isSuspended(); }
          public boolean isPossible() { return true; }
    };
    suspendControl = new OnOffButtonControl(pauseResumeModelConfig, skin) {
      public void syncWithModel() {
        super.syncWithModel();
        toggleButton.setText(science2DStage.isSuspended() ? "Resume" : "Pause");
      }
    };

    // Add reset functionality for the experiment
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>("Reset", "Reset to initial state") {
          public void doCommand() { science2DModel.reset(); }
          public boolean isPossible() { return true; }
    };
    IControl resetControl = new CommandButtonControl(resetModelConfig, skin);
    
    suspendResetPanel = new Table(skin);
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
  
  public String getExperimentName() {
    return experimentName;
  }

  public void enableControls(boolean enable) {
    suspendResetPanel.visible = enable;
    modelControlPanel.touchable = enable;
    this.invalidate();
  }
}