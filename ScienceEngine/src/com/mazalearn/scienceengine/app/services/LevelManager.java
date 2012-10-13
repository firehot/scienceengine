package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public class LevelManager {
  private IScience2DStage science2DStage;
  //private FileHandle file;
  private int level = 1;
  private ControlPanel controlPanel;
  private IScience2DModel science2DModel;
  private LevelLoader levelLoader;
  private String description = "";

  public LevelManager(IScience2DStage stage, IScience2DModel science2DModel, ControlPanel controlPanel) {
    this.science2DStage = stage;
    this.science2DModel = science2DModel;
    this.controlPanel = controlPanel;
  }

  public String getName() {
    return controlPanel.getExperimentName();
  }

  public int getLevel() {
    return level;
  }
  
  public void setLevel(int level) {
    this.level = level;
  }

  public String getDescription() {
    return description;
  }
  
  /**
   * Loads the content of the provided file and automatically position and size
   * the objects.
   * @param index 
   */
  public void load() {
    try {
      LevelLoader levelLoader = new LevelLoader(level, 
          controlPanel, science2DStage, science2DModel);
      levelLoader.load();
      this.description = levelLoader.getDescription();
      Gdx.app.log(ScienceEngine.LOG, "[LevelEditor] Level successfully loaded!");
    } catch (GdxRuntimeException ex) {
      System.err.println("[LevelEditor] Error happened while loading level");
    }
    controlPanel.refresh();
  }

  public static Texture getThumbnail(String experimentName, int level) {
    FileHandle screenFile = 
        Gdx.files.internal(LevelManager.getFileName(experimentName, ".png", level));      
    Pixmap pixmap;
    try {
      pixmap = new Pixmap(screenFile);
    } catch (GdxRuntimeException e) {
      pixmap = new Pixmap(powerOf2Ceiling(Gdx.graphics.getWidth()/7.5f), 
          powerOf2Ceiling(Gdx.graphics.getHeight()/7.5f), 
          Format.RGBA8888);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  public void setDescription(String description) {
    levelLoader.setDescription(description);
  }

  public static int powerOf2Ceiling(float value) {
    return 1 << (int) Math.ceil(Math.log(value) / Math.log(2));
  }

  public static String getFileName(String experimentName, String extension, int level) {
    return "data/" + experimentName + "/" + level + extension;
  }
}