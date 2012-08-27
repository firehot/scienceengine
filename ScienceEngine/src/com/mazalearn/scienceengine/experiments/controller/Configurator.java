package com.mazalearn.scienceengine.experiments.controller;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.model.IExperimentModel;
import com.mazalearn.scienceengine.experiments.view.IExperimentView;
import com.mazalearn.scienceengine.screens.StartScreen;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

public class Configurator extends Table {
  final IExperimentModel experimentModel;
  final IExperimentView experimentView;
  final Skin skin;
  List<Config> configs;
  
  public Configurator(Skin skin, final IExperimentModel experimentModel, 
      final IExperimentView experimentView) {
    super(skin);
    this.skin = skin;
    this.experimentModel = experimentModel;
    this.experimentView = experimentView;
    this.configs = new ArrayList<Config>();
    registerStandardButtons(skin, experimentModel, experimentView);
    registerModelConfigs(experimentModel);    
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
    // register the back button
    TextButton backButton = new TextButton("Back to Start", skin);
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        ScienceEngine.GAME.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.GAME.setScreen(new StartScreen(ScienceEngine.GAME));
      }
    });
    add(backButton).height(30).colspan(2);
    row();
    
    // Add pause/resume functionality for the experiment
    final TextButton pauseResumeButton = new TextButton("Pause", skin);
    pauseResumeButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        if (experimentView.isPaused()) {
          pauseResumeButton.setText("Pause");
          experimentView.resume();
        } else {
          pauseResumeButton.setText("Resume");
          experimentView.pause();
        }
      }
    });
    this.add(pauseResumeButton);
    
    // Add reset functionality for the experiment
    createViewConfig(new AbstractModelConfig<String>("Reset", "Reset to initial conditions") {
      public void doCommand() { experimentModel.reset(); }
    });
    row();
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Config createViewConfig(IModelConfig property) {
    Table table = new Table(skin);
    IViewConfig viewConfig = null;
    switch(property.getType()) {
      case Float: 
        table.add(property.getName());
        table.row();
        viewConfig = new ConfigSlider(property, skin);
        break;
      case String:
        viewConfig = new ConfigSelectBox(property, skin);
        break;
      case Command:
        viewConfig = new ConfigTextButton(property, skin);
        break;
    }
    table.add(viewConfig.getActor());
    this.add(table); this.row();
    Config config = new Config(table, viewConfig);
    this.configs.add(config);
    return config;
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