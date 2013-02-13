package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
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
    image.setSize(ScreenComponent.getScaledX(image.getWidth()),
        ScreenComponent.getScaledY(image.getHeight()));
    image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
    image.setOrigin(0, image.getHeight() / 2);
    return image;
  }
    
  private Image createResultImage(String path, float scale) {
    Image image = new Image(new Texture(path));
    image.setSize(ScreenComponent.getScaledX(image.getWidth()),
        ScreenComponent.getScaledY(image.getHeight()));
    image.setVisible(false);
    image.setSize(image.getWidth() * scale, image.getHeight() * scale);
    image.setOrigin(0, image.getHeight() / 2);
    return image;
  }
  
  public void done(boolean success) {
    netSuccesses += success ? 1 : -1;
    if (success) {
      guru.showCorrect(getSuccessScore());
    } else {
      guru.showWrong(getFailureScore());
      setSuccessScore(getFailureScore()); // Equate success and failure scores
      return;
    }
    dummy.setConfigParameter(null, 0);
    science2DController.getGuru().setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), true);
    image.setVisible(false);
    ScienceEngine.setProbeMode(false);
    super.done(success);
  }
    
  public ParameterProber(IScience2DController science2DController,
      ITutor parent, String goal, String name, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, parent, goal, name, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    this.image = new ProbeImage();
    
    dummy = (DummyBody) science2DController.getModel().findBody(ComponentType.Dummy);
  }
  
  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
    image.setVisible(false);
  }
  
  @Override
  public void teach() {
    super.teach();
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
    ScienceEngine.selectBody(dummy, science2DController.getView());
    image.setVisible(true);
    ScienceEngine.setProbeMode(true);
  }
  
  @Override
  public void checkProgress() {
  }
  
  public void initialize(IModelConfig<?> probeConfig, 
      String resultExprString, String resultType) {
    this.probeConfig = probeConfig;

    this.resultType = ResultType.valueOf(resultType);
    if (this.resultType == ResultType.Spin) {   
      image.setX(ScreenComponent.VIEWPORT_WIDTH / 2 - image.getWidth() / 2 - ScreenComponent.getScaledX(50));
      image.setY(ScreenComponent.VIEWPORT_HEIGHT / 2 - image.getHeight() / 2);
      
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
      image.setX(ScreenComponent.getScaledX(700) - image.getWidth()/2);
      image.setY(ScreenComponent.getScaledY(250) - image.getHeight()/2);
     
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
      this.addActor(decrease);
      this.addActor(increase);
      this.addActor(dontCare);      
    }
    this.addActor(image);
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
}