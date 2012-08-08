package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.MoleculeBox;

/**
 * Experiment screen.
 */
public class ExperimentScreen extends AbstractScreen {

  final String experimentName;
  final Actor experiment;

  public ExperimentScreen(
      ScienceEngine game, Actor experiment, String experimentName) {
    super(game);
    this.experimentName = experimentName;
    this.experiment = experiment;
  }

  @Override
  public void show() {
    super.show();
    
    // retrieve the default table actor
    Table table = super.getTable();
    table.add(experimentName).spaceBottom(50);
    table.row();
    
    // Add experiment to table
    table.add(experiment)
        .width(GAME_VIEWPORT_WIDTH)
        .height(GAME_VIEWPORT_HEIGHT);
  }
  
  @Override
  public void dispose() {
    super.dispose();
  }    
}
