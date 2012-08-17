package com.mazalearn.scienceengine.experiments.config;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.experiments.Experiment;

public class Configurator extends Table {
  final Experiment experiment;
  final Skin skin;
  List<Config> configs;
  
  public Configurator(Skin skin, final Experiment experiment) {
    super(skin);
    this.skin = skin;
    this.experiment = experiment;
    this.configs = new ArrayList<Config>();
    addButton("Pause").addCondition(new Condition() {
      public boolean eval() {
        return !experiment.isPaused();
      }
    });
    addButton("Resume").addCondition(new Condition() {
      public boolean eval() {
        return experiment.isPaused();
      }
    });
    addButton("Reset");
  }
  
  public Config addButton(String caption) {
    Table table = new Table(skin);
    table.add(new ConfigTextButton(experiment, caption, skin));
    this.add(table); this.row();
    Config config = new Config(table);
    this.configs.add(config);
    return config;
  }
  
  public Config addSlider(String property, float low, float high) {
    Table table = new Table(skin);
    table.add(property);
    table.row();
    table.add(new ConfigSlider(experiment, property, low, high, skin));
    this.add(table); this.row();
    Config config = new Config(table);
    this.configs.add(config);
    return config;
  }

  public Config addSelect(String property, String[] items) {
    Table table = new Table(skin);
    table.add(new ConfigSelectBox(experiment, property, items, skin));
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