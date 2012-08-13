package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.StatesOfMatter;
import com.mazalearn.scienceengine.experiments.WaveMotion;

/**
 * Experiment screen.
 */
public class ExperimentScreen extends AbstractScreen {

  final String experimentName;

  public ExperimentScreen(ScienceEngine game, String experimentName) {
    super(game);
    this.experimentName = experimentName;
  }

  @Override
  public void show() {
    super.show();
    
    // retrieve the default table actor
    Table table = super.getTable();
    table.add(experimentName).spaceBottom(10);
    table.row();
    
    // Add States of Matter experiment to table
    Actor experiment = null;
    if (experimentName == "States Of Matter") {
      experiment = new StatesOfMatter(getSkin());
    } else if (experimentName == "Wave Motion") {
      experiment = new WaveMotion(getSkin());
    }
    table.add(experiment)
        .width(GAME_VIEWPORT_WIDTH)
        .height(GAME_VIEWPORT_HEIGHT);
  }
  
  @Override
  public void dispose() {
    super.dispose();
  }    
}
