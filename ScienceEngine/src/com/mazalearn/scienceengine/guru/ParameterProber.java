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
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.DummyBody;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

// outcome = function of parameter
// doubts on how parameter change affects outcome
// Generate a parameter point.
// Is the outcome given by expression true?
public class ParameterProber extends AbstractScience2DProber {
  
  enum Type {
    Spin,
    Direct,
    Inverse,
    None;
  }
  
  private String[] hints = new String[] {
      "Use Fleming's left hand rule"
  };
  
  protected Type type;
  protected Image image;
  protected ClickResult imageListener;

  protected String title;

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
    
  public ParameterProber(IScience2DModel science2DModel, IScience2DView science2DView,
      String type, int deltaSuccessScore, int deltaFailureScore) {
    super(science2DModel, science2DView, deltaSuccessScore, deltaFailureScore);
    this.image = new ProbeImage();
    this.type = Type.valueOf(type);
    Guru guru = science2DView.getGuru();
    if (this.type == Type.Spin) {   
      image.setX(guru.getWidth() / 2 - image.getWidth() / 2 - 50);
      image.setY(guru.getHeight() / 2 - image.getHeight() / 2);
      
      Image clockwise = createResultImage("images/clockwise.png", 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      Image dontCare = createResultImage("images/cross.png", 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      Image antiClockwise = createResultImage("images/anticlockwise.png", 
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

      imageListener = new ClickResult(guru, new Image[] {decrease, increase, dontCare},
          new ClickResult.StateMapper() {
        @Override
        public int map(float x, float y) {
          if (x < image.getWidth() / 2 && y > image.getHeight() / 2) return 0;
          if (x > image.getWidth() / 2 && y > image.getHeight() / 2) return 1;
          return 2;
        }
      });

      switch (this.type) {
        case None: imageListener.setResult(2); break;
        case Direct: imageListener.setResult(1); break;
        case Inverse: imageListener.setResult(0); break;
      }
      this.addActor(decrease);
      this.addActor(increase);
      this.addActor(dontCare);      
    }

    dummy = (DummyBody) science2DModel.findBody(ComponentType.Dummy);
    this.addActor(image);
    image.addListener(imageListener);   
  }
  
  @Override
  public String getTitle() {
    return title;
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image.setVisible(false);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      if (type == Type.Spin) {
        List<IModelConfig<?>> configs = new ArrayList<IModelConfig<?>>();
        configs.add(probeConfig);
        science2DView.getGuru().setupProbeConfigs(configs, false);
        science2DModel.bindParameterValues(resultExprVariables);
        imageListener.setResult(resultExpr.fvalue() == 1 ? 0 : 1);
      } else {
        float value = MathUtils.random(0f, 10f);
        dummy.setConfigParameter(probeConfig.getParameter(), value);
        science2DView.getGuru().setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
      }
    } else {
      dummy.setConfigParameter(null, 0);
    }
    image.setVisible(activate);
    ScienceEngine.setProbeMode(activate);
    ScienceEngine.selectBody(null, null);
    this.setVisible(activate);
  }
  
  @Override
  public String getHint() {
    return hints[0];
  }

  @Override
  public void checkProgress() {
  }
  
  public void initialize(String title, IModelConfig<?> probeConfig, 
      String resultExprString, String type, Array<?> components, 
      Array<?> configs) {
    super.initialize(components, configs);
    this.title = title;
    this.probeConfig = probeConfig;
    this.configs = configs;
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
  public boolean isCompleted() {
    return true;
  }

}