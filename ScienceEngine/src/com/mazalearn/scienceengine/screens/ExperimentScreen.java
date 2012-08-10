package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.StatesOfMatter;

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
    table.add(new TextButton(experimentName, getSkin())).spaceBottom(10);
    table.row();
    
    // Add experiment to table
    table.add(new StatesOfMatter(getSkin()))
        .width(GAME_VIEWPORT_WIDTH)
        .height(GAME_VIEWPORT_HEIGHT);
  }
  
  @Override
  public void dispose() {
    super.dispose();
  }    
}
