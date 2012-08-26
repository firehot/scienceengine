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
  final IExperimentModel iExperimentModel;
  final IExperimentView iExperimentView;
  final Skin skin;
  List<Config> configs;
  
  public Configurator(Skin skin, final IExperimentModel iExperimentModel, 
      final IExperimentView iExperimentView) {
    super(skin);
    this.skin = skin;
    this.iExperimentModel = iExperimentModel;
    this.iExperimentView = iExperimentView;
    this.configs = new ArrayList<Config>();
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
        if (iExperimentView.isPaused()) {
          pauseResumeButton.setText("Pause");
          iExperimentView.resume();
        } else {
          pauseResumeButton.setText("Resume");
          iExperimentView.pause();
        }
      }
    });
    this.add(pauseResumeButton);
    // Add reset functionality for the experiment
    addButton(new AbstractConfig<String>("Reset", "Reset to initial conditions") {
      public void doCommand() { iExperimentModel.reset(); }
    });
    row();
  }
  
  public Config addButton(IConfig<String> command) {
    Table table = new Table(skin);
    ConfigTextButton configTextButton = new ConfigTextButton(command, skin);
    table.add(configTextButton);
    this.add(table); this.row();
    Config config = new Config(table, configTextButton);
    this.configs.add(config);
    return config;
  }
  
  public Config addSlider(IConfig<Float> property) {
    Table table = new Table(skin);
    table.add(property.getName());
    table.row();
    ConfigSlider configSlider = new ConfigSlider(property, skin);
    table.add(configSlider);
    this.add(table); this.row();
    Config config = new Config(table, configSlider);
    this.configs.add(config);
    return config;
  }

  public Config addSelect(IConfig<String> property) {
    Table table = new Table(skin);
    ConfigSelectBox configSelectBox = new ConfigSelectBox(property, skin);
    table.add(configSelectBox);
    this.add(table); this.row();
    Config config = new Config(table, configSelectBox);
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