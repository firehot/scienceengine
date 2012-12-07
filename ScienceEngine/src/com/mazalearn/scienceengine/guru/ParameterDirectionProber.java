package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelLoader;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

// outcome = function of parameter
// doubts on how parameter change affects outcome
// Generate a parameter point.
// Is the outcome given by expression true?
public class ParameterDirectionProber extends AbstractScience2DProber {
  
  enum Type {
    Spin,
    None;
  }
  
  private Stage[] stages = new Stage[] {
      new Stage("Use Fleming's left hand rule")
  };
  
  private final Image image;
  private final Image clockwise, antiClockwise, dontCare;
  private ClickResult imageListener;

  private Array<?> configs;

  private IScience2DModel science2DModel;

  private String title;

  private Expr resultExpr;

  private Set<Variable> resultExprVariables;

  private IModelConfig<?> probeConfig;
  
  private Image createResultImage(String path, float x, float y) {
    Image image = new Image(new Texture(path));
    image.setVisible(false);
    image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
    return image;
  }
    
  public ParameterDirectionProber(IScience2DModel science2DModel, Guru guru) {
    super(guru);
    this.science2DModel = science2DModel;
    
    image = new ProbeImage();
    image.setX(guru.getWidth() / 2 - image.getWidth() / 2 - 50);
    image.setY(guru.getHeight() / 2 - image.getHeight() / 2);
    
    clockwise = createResultImage("images/clockwise.png", 
        image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
    dontCare = createResultImage("images/cross.png", 
        image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
    antiClockwise = createResultImage("images/anticlockwise.png", 
        image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);

    imageListener = new ClickResult(guru, new Image[] {clockwise, antiClockwise, dontCare},
        new ClickResult.StateMapper() {
      @Override
      public int map(float x, float y) {
        if (x < image.getWidth() / 2 && y > 0) return 0;
        if (x > image.getWidth() / 2&& y > 0) return 1;
        return 2;
      }
    });
    image.addListener(imageListener);   

    this.addActor(image);
    this.addActor(clockwise);
    this.addActor(antiClockwise);
    this.addActor(dontCare);
  }
  
  @Override
  public String getTitle() {
    return title;
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image.setVisible(false);
    LevelLoader.readConfigs(configs, science2DModel);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      List<IModelConfig<?>> configs = new ArrayList<IModelConfig<?>>();
      configs.add(probeConfig);
      guru.setupProbeConfigs(configs, false);
      science2DModel.bindParameterValues(resultExprVariables);
      imageListener.setResult(resultExpr.fvalue() == 1 ? clockwise : antiClockwise);
      image.setVisible(true);
    } 
    ScienceEngine.setProbeMode(activate);
    ScienceEngine.selectBody(null, null);
    this.setVisible(activate);
  }
  
  @Override
  public String getHint() {
    return stages[0].getHint();
  }

  public void setProbeConfig(String title, IModelConfig<?> probeConfig, 
      String resultExprString, String type, Array<?> configs) {
    this.title = title;
    this.probeConfig = probeConfig;
    Parser parser = new Parser();
    try {
      this.resultExpr = parser.parseString(resultExprString);
    } catch (SyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.resultExprVariables = parser.getVariables();
    this.configs = configs;
  }
}