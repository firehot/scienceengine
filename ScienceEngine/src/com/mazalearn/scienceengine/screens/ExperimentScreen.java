package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.ElectroMagnetismExperiment;
import com.mazalearn.scienceengine.experiments.StatesOfMatterExperiment;
import com.mazalearn.scienceengine.experiments.WaveExperiment;

/**
 * IExperimentModel screen.
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
    
    // Add States of Matter iExperimentModel to table
    Actor experiment = null;
    if (experimentName == "States of Matter") {
      experiment = new StatesOfMatterExperiment(getSkin());
    } else if (experimentName == "Wave Motion") {
      experiment = new WaveExperiment(getAtlas(), getSkin());
    } else if (experimentName == "Electromagnetism") {
      experiment = new ElectroMagnetismExperiment(getSkin());
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
