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
import com.mazalearn.scienceengine.core.controller.Config;
import com.mazalearn.scienceengine.core.controller.ConfigCheckBox;
import com.mazalearn.scienceengine.core.controller.ConfigSelectBox;
import com.mazalearn.scienceengine.core.controller.ConfigSlider;
import com.mazalearn.scienceengine.core.controller.ConfigTextButton;
import com.mazalearn.scienceengine.core.controller.IExperimentController;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IViewConfig;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.IExperimentView;

public class ControlPanel extends Table {
  private final IExperimentController experimentController;
  private final IExperimentModel experimentModel;
  private final IExperimentView experimentView;
  private final Skin skin;
  private List<Config> configs;
  private String experimentName;
  private IViewConfig pauseResumeConfig;
  private IViewConfig challengeConfig;
  private Table modelControlPanel;
  private Label title;
  
  public ControlPanel(Skin skin, IExperimentController experimentController) {
    super(skin, null, experimentController.getName());
    this.skin = skin;
    this.experimentController = experimentController;
    this.experimentModel = experimentController.getModel();
    this.experimentView = experimentController.getView();
    this.experimentName = experimentController.getName();
    this.defaults().fill();
    Actor standardControlPanel = 
        createStandardControlPanel(skin, experimentModel, experimentView);
    this.modelControlPanel = createModelControlPanel(skin);
    this.add(standardControlPanel);
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
    return experimentModel.getAllConfigs();
  }

  @SuppressWarnings("rawtypes")
  protected void registerModelConfigs(Table modelControlPanel) {
    this.configs = new ArrayList<Config>();
    modelControlPanel.clear();
    // Register all model configs
    for (IModelConfig modelConfig: experimentModel.getAllConfigs()) {
      this.configs.add(createViewConfig(modelConfig, modelControlPanel));
    }
  }

  protected Actor createStandardControlPanel(Skin skin,
      final IExperimentModel experimentModel,
      final IExperimentView experimentView) {
    Table standardControls = new Table(skin, null, "Standard");
    standardControls.defaults().fill();
    // Register name
    this.title = new Label(experimentName, skin);
    standardControls.add(title).colspan(2).center();
    standardControls.row();
    // register the back button
    TextButton backButton = new TextButton("Back", skin);
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        ScienceEngine.SCIENCE_ENGINE.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.SCIENCE_ENGINE.setScreen(
            new ExperimentHomeScreen(ScienceEngine.SCIENCE_ENGINE, experimentController));
      }
    });
    standardControls.add(backButton).height(30).colspan(2);
    standardControls.row();
    
    // Add challenge/learn functionality
    AbstractModelConfig<String> challengeModelConfig = 
        new AbstractModelConfig<String>("Challenge/Learn", "Challenge or Learn") {
          public void doCommand() { 
            experimentView.challenge(!experimentView.isChallengeInProgress());
          }
          public boolean isPossible() { return true; }
    };
    
    challengeConfig = new ConfigTextButton(challengeModelConfig, skin) {
      public void syncWithModel() {
        textButton.setText(
            experimentView.isChallengeInProgress() ? "Learn" : "Challenge");
      }
    };
    standardControls.add(challengeConfig.getActor()).height(30).colspan(2);
    standardControls.row();
    
    // Add pause/resume functionality for the experiment
    AbstractModelConfig<String> pauseResumeModelConfig = 
        new AbstractModelConfig<String>("Pause/Resume", "Pause or Resume") {
          public void doCommand() { 
            if (experimentView.isPaused()) {
              experimentView.resume();
            } else {
              experimentView.pause();
            }
          }
          public boolean isPossible() { return true; }
    };
    pauseResumeConfig = new ConfigTextButton(pauseResumeModelConfig, skin) {
      public void syncWithModel() {
        textButton.setText(experimentView.isPaused() ? "Resume" : "Pause");
      }
    };

    // Add reset functionality for the experiment
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>("Reset", "Reset to initial state") {
          public void doCommand() { experimentModel.reset(); }
          public boolean isPossible() { return true; }
    };
    IViewConfig resetConfig = new ConfigTextButton(resetModelConfig, skin);
    
    Table suspendResetTable = new Table(skin);
    suspendResetTable.defaults().fill().expand();
    suspendResetTable.add(pauseResumeConfig.getActor()).pad(0,5,0, 5);
    suspendResetTable.add(resetConfig.getActor());
    
    standardControls.add(suspendResetTable).pad(0, 0, 10, 0);
    standardControls.row();
    return standardControls;
  }
  
  public Label getTitle() {
    return this.title;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Config createViewConfig(IModelConfig property, Table modelConfigTable) {
    Table table = new Table(skin);
    table.defaults().fill().expand();
    IViewConfig viewConfig = null;
    switch(property.getType()) {
      case ONOFF: 
        viewConfig = new ConfigCheckBox(property, skin);
        table.add(viewConfig.getActor());
        table.add(property.getName()).pad(0, 5, 0, 5);
        break;
      case RANGE: 
        table.add(property.getName());
        table.row();
        viewConfig = new ConfigSlider(property, skin);
        table.add(viewConfig.getActor());
        break;
      case LIST:
        viewConfig = new ConfigSelectBox(property, skin);
        table.add(viewConfig.getActor());
        break;
      case COMMAND:
        viewConfig = new ConfigTextButton(property, skin);
        table.add(viewConfig.getActor());
        break;
    }
    Config c = new Config(modelConfigTable.add(table), viewConfig);
    modelConfigTable.row();
    return c;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    challengeConfig.syncWithModel();
    pauseResumeConfig.syncWithModel();
    for (Config config: configs) {
      config.validate();
    }
    this.invalidate();
    this.validate();
  }
  
  public String getExperimentName() {
    return experimentName;
  }
}