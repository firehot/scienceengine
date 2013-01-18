package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.DummyBody;

// outcome = function of parameter
// doubts on how parameter change affects outcome
// Generate a parameter point.
// Is the outcome given by expression true?
public class ParameterProber extends AbstractScience2DProber implements IDoneCallback {
  
  protected int netSuccesses;
  enum ResultType {
    Spin,
    Direct,
    Inverse,
    None;
  }
  
  
  private ResultType resultType;
  private Image image;
  private ClickResult imageListener;

  private Expr resultExpr;

  private Set<Variable> resultExprVariables;

  protected IModelConfig<?> probeConfig;

  protected DummyBody dummy;
  
  private Image createResultImage(String path, float x, float y) {
    Image image = new Image(new Texture(path));
    image.setVisible(false);
    image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
    image.setOrigin(0, image.getHeight() / 2);
    return image;
  }
    
  private Image createResultImage(String path, float scale) {
    Image image = new Image(new Texture(path));
    image.setVisible(false);
    image.setSize(image.getWidth() * scale, image.getHeight() * scale);
    image.setOrigin(0, image.getHeight() / 2);
    return image;
  }
  
  public void done(boolean success) {
    netSuccesses += success ? 1 : -1;
    science2DController.getGuru().done(success);
  }
    
  public ParameterProber(IScience2DController science2DController,
      String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore) {
    super(science2DController, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    this.image = new ProbeImage();
    
    dummy = (DummyBody) science2DController.getModel().findBody(ComponentType.Dummy);
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image.setVisible(false);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      if (resultType == ResultType.Spin) {
        List<IModelConfig<?>> configs = new ArrayList<IModelConfig<?>>();
        configs.add(probeConfig);
        science2DController.getGuru().setupProbeConfigs(configs, false);
        science2DController.getModel().bindParameterValues(resultExprVariables);
        imageListener.setResult(resultExpr.bvalue() ? 0 : 1);
      } else {
        float value = MathUtils.random(0f, 10f);
        dummy.setConfigParameter(probeConfig.getParameter(), value);
        science2DController.getGuru().setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
      }
    } else {
      dummy.setConfigParameter(null, 0);
      science2DController.getGuru().setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), true);
    }
    image.setVisible(activate);
    ScienceEngine.setProbeMode(activate);
    ScienceEngine.selectBody(dummy, science2DController.getView());
    // Turn on access to disabled parts of control panel
    this.setVisible(activate);
  }
  
  @Override
  public void checkProgress() {
  }
  
  public void initialize(IModelConfig<?> probeConfig, 
      String resultExprString, String resultType, String[] hints) {
    this.probeConfig = probeConfig;
    this.hints = hints;

    Group root = ((Stage)science2DController.getView()).getRoot();
    Actor controlPanel = root.findActor("ControlPanel");
    this.resultType = ResultType.valueOf(resultType);
    if (this.resultType == ResultType.Spin) {   
      image.setX(science2DController.getGuru().getWidth() / 2 - image.getWidth() / 2 - 50);
      image.setY(science2DController.getGuru().getHeight() / 2 - image.getHeight() / 2);
      
      Image clockwise = createResultImage("images/clockwise.png", 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      Image dontCare = createResultImage("images/cross.png", 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      Image antiClockwise = createResultImage("images/anticlockwise.png", 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
  
      imageListener = new ClickResult(this, new Image[] {clockwise, antiClockwise, dontCare},
          new ClickResult.StateMapper() {
        @Override
        public int map(float x, float y) {
          if (x < image.getWidth() / 2 && y > 0) return 0;
          if (x > image.getWidth() / 2&& y > 0) return 1;
          return 2;
        }
      });
      this.addActor(clockwise);
      this.addActor(antiClockwise);
      this.addActor(dontCare);      
    } else {
      image.setX(700 - image.getWidth()/2);
      image.setY(175 - image.getHeight()/2);
     
      Image decrease = createResultImage("images/fieldarrow.png", 2);
      decrease.setPosition(image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 3);
      decrease.setRotation(180);
      Image dontCare = createResultImage("images/cross.png", 1);
      dontCare.setPosition(image.getX() + image.getWidth() / 2 - dontCare.getWidth()/2, 
          image.getY() + image.getHeight() / 2 - dontCare.getHeight()/2);
      Image increase = createResultImage("images/fieldarrow.png", 2);
      increase.setPosition(image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 3);

      imageListener = new ClickResult(this, new Image[] {decrease, increase, dontCare},
          new ClickResult.StateMapper() {
        @Override
        public int map(float x, float y) {
          if (x < image.getWidth() / 2 && y > image.getHeight() / 2) return 0;
          if (x > image.getWidth() / 2 && y > image.getHeight() / 2) return 1;
          return 2;
        }
      });

      switch (this.resultType) {
        case None: imageListener.setResult(2); break;
        case Direct: imageListener.setResult(1); break;
        case Inverse: imageListener.setResult(0); break;
      }
      root.addActorAfter(controlPanel, decrease);
      root.addActorAfter(controlPanel, increase);
      root.addActorAfter(controlPanel, dontCare);      
    }
    root.addActorAfter(controlPanel, image);
    image.addListener(imageListener);   

    if (resultExprString == null) return;
    Parser parser = new Parser();
    try {
      this.resultExpr = parser.parseString(resultExprString);
    } catch (SyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.resultExprVariables = parser.getVariables();
    
  }

  @Override
  public boolean hasSucceeded() {
    return netSuccesses >= 1;
  }


  @Override
  public boolean hasFailed() {
    return false; // Allow learner to keep trying forever
  }

}