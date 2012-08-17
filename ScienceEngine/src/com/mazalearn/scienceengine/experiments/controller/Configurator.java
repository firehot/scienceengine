package com.mazalearn.scienceengine.experiments.controller;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.experiments.model.ExperimentModel;
import com.mazalearn.scienceengine.experiments.view.ExperimentView;

public class Configurator extends Table {
  final ExperimentModel experimentModel;
  final ExperimentView experimentView;
  final Skin skin;
  List<Config> configs;
  
  public Configurator(Skin skin, final ExperimentModel experimentModel, 
      final ExperimentView experimentView) {
    super(skin);
    this.skin = skin;
    this.experimentModel = experimentModel;
    this.experimentView = experimentView;
    this.configs = new ArrayList<Config>();
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
    addButton("Reset");
  }
  
  public Config addButton(String caption) {
    Table table = new Table(skin);
    table.add(new ConfigTextButton(experimentModel, caption, skin));
    this.add(table); this.row();
    Config config = new Config(table);
    this.configs.add(config);
    return config;
  }
  
  public Config addSlider(String property, float low, float high) {
    Table table = new Table(skin);
    table.add(property);
    table.row();
    table.add(new ConfigSlider(experimentModel, property, low, high, skin));
    this.add(table); this.row();
    Config config = new Config(table);
    this.configs.add(config);
    return config;
  }

  public Config addSelect(String property, String[] items) {
    Table table = new Table(skin);
    table.add(new ConfigSelectBox(experimentModel, property, items, skin));
    this.add(table); this.row();
    Config config = new Config(table);
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