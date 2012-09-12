package com.mazalearn.scienceengine.controller;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
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
  
  public Configurator(Skin skin, final IExperimentModel experimentModel, 
      final IExperimentView experimentView, final String experimentName) {
    super(skin, null, experimentName);
    this.skin = skin;
    this.experimentModel = experimentModel;
    this.experimentView = experimentView;
    this.configs = new ArrayList<Config>();
    this.experimentName = experimentName;
    registerStandardButtons(skin, experimentModel, experimentView);
    registerModelConfigs(experimentModel);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
  }

  @SuppressWarnings("rawtypes")
  protected void registerModelConfigs(final IExperimentModel experimentModel) {
    // Register all model configs
    for (IModelConfig modelConfig: experimentModel.getConfigs()) {
      this.configs.add(createViewConfig(modelConfig));
    }
  }

  protected void registerStandardButtons(Skin skin,
      final IExperimentModel experimentModel,
      final IExperimentView experimentView) {
    // Register name
    add(experimentName).colspan(2).center();
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
    
    // Register the game button
    final TextButton challengeButton = new TextButton("Challenge Me!", skin);
    challengeButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        ScienceEngine.SCIENCE_ENGINE.getSoundManager().play(ScienceEngineSound.CLICK);
        if (experimentView.isChallengeInProgress()) {
          experimentView.challenge(false);
          challengeButton.setText("Challenge Me!");
        } else {
          experimentView.challenge(true);
          challengeButton.setText("Learn");
        }
      }
    });
    this.add(challengeButton).height(30).colspan(2);
    row();
    
    // Add pause/resume functionality for the experiment
    final TextButton pauseResumeButton = new TextButton("Pause", skin);
    pauseResumeButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        ScienceEngine.SCIENCE_ENGINE.getSoundManager().play(ScienceEngineSound.CLICK);
        if (experimentView.isPaused()) {
          pauseResumeButton.setText("Pause");
          experimentView.resume();
        } else {
          pauseResumeButton.setText("Resume");
          experimentView.pause();
        }
      }
    });
    
    // Add reset functionality for the experiment
    AbstractModelConfig<String> resetModelConfig = 
        new AbstractModelConfig<String>("Reset", "Reset to initial state") {
          public void doCommand() { experimentModel.reset(); }
          public boolean isPossible() { return true; }
    };
    IViewConfig resetConfig = new ConfigTextButton(resetModelConfig, skin);
    
    Table table = new Table(skin);
    table.add(pauseResumeButton).pad(0,5,0, 5);
    table.add(resetConfig.getActor());
    this.add(table);
    row();
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Config createViewConfig(IModelConfig property) {
    Table table = new Table(skin);
    IViewConfig viewConfig = null;
    switch(property.getType()) {
      case RANGE: 
        table.add(property.getName());
        table.row();
        viewConfig = new ConfigSlider(property, skin);
        break;
      case LIST:
        viewConfig = new ConfigSelectBox(property, skin);
        break;
      case COMMAND:
        viewConfig = new ConfigTextButton(property, skin);
        break;
    }
    table.add(viewConfig.getActor());
    Config c = new Config(this.add(table), viewConfig);
    this.row();
    return c;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    for (Config config: configs) {
      config.validate();
    }
    this.invalidate();
    this.validate();
    super.draw(batch, parentAlpha);
  }
}