package com.mazalearn.scienceengine.controller;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.screens.StartScreen;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.view.IExperimentView;

public class Configurator extends Table {
  final IExperimentModel experimentModel;
  final IExperimentView experimentView;
  final Skin skin;
  List<Config> configs;
  private String experimentName;
  private IViewConfig pauseResumeConfig;
  private IViewConfig challengeConfig;
  private Table modelConfigTable;
  private Label title;
  
  public Configurator(Skin skin, final IExperimentModel experimentModel, 
      final IExperimentView experimentView, final String experimentName) {
    super(skin, null, experimentName);
    this.skin = skin;
    this.experimentModel = experimentModel;
    this.experimentView = experimentView;
    this.experimentName = experimentName;
    this.defaults().fill();
    registerStandardButtons(skin, experimentModel, experimentView);
    this.modelConfigTable = new Table(skin);
    modelConfigTable.defaults().fill();
    this.add(modelConfigTable);
    registerModelConfigs();
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
  }
  
  public void refresh() {
    registerModelConfigs();
  }

  @SuppressWarnings("rawtypes")
  protected void registerModelConfigs() {
    this.configs = new ArrayList<Config>();
    modelConfigTable.clear();
    // Register all model configs
    for (IModelConfig modelConfig: experimentModel.getAllConfigs()) {
      this.configs.add(createViewConfig(modelConfig, modelConfigTable));
    }
  }

  protected void registerStandardButtons(Skin skin,
      final IExperimentModel experimentModel,
      final IExperimentView experimentView) {
    // Register name
    this.title = new Label(experimentName, skin);
    add(title).colspan(2).center();
    row();
    // register the back button
    TextButton backButton = new TextButton("Back to Start", skin);
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        ScienceEngine.SCIENCE_ENGINE.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.SCIENCE_ENGINE.setScreen(new StartScreen(ScienceEngine.SCIENCE_ENGINE));
      }
    });
    this.add(backButton).height(30).colspan(2);
    row();
    
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
    this.add(challengeConfig.getActor()).height(30).colspan(2);
    row();
    
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
    
    Table table = new Table(skin);
    table.defaults().fill().expand();
    table.add(pauseResumeConfig.getActor()).pad(0,5,0, 5);
    table.add(resetConfig.getActor());
    this.add(table).pad(0, 0, 10, 0);
    row();
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
  public void draw(SpriteBatch batch, float parentAlpha) {
    challengeConfig.syncWithModel();
    pauseResumeConfig.syncWithModel();
    for (Config config: configs) {
      config.validate();
    }
    this.invalidate();
    this.validate();
    super.draw(batch, parentAlpha);
  }
}