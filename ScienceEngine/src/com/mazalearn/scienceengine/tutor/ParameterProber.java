package com.mazalearn.scienceengine.tutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.view.IScience2DView;

// outcome = function of parameter
// doubts on how parameter change affects outcome
// Generate a parameter point.
// Is the outcome given by expression true?
public class ParameterProber extends AbstractScience2DProber {
  
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
  
  private Image createResultImage(TextureRegion textureRegion, float x, float y) {
    Image image = new Image(textureRegion);
    image.setVisible(false);
    ScreenComponent.scaleSize(image, image.getWidth(), image.getHeight());
    image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
    image.setOrigin(0, image.getHeight() / 2);
    return image;
  }
    
  private Image createResultImage(TextureRegion textureRegion, float scale) {
    Image image = new Image(textureRegion);
    ScreenComponent.scaleSize(image, image.getWidth(), image.getHeight());
    //image.setVisible(false);
    image.setSize(image.getWidth() * scale, image.getHeight() * scale);
    image.setOrigin(0, image.getHeight() / 2);
    return image;
  }
  
  @Override 
  public void finish() {
    ScienceEngine.clearPins();
    science2DController.getGuru().setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), true);
    image.setVisible(false);
    ScienceEngine.setProbeMode(false);
    super.finish();
  }
    
  public ParameterProber(IScience2DController science2DController,
      TutorType tutorType, ITutor parent, String goal, String name, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, tutorType, parent, goal, name, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    this.image = new ProbeImage();
  }
  
  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
    image.setVisible(false);
    ScienceEngine.clearPins();
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
      science2DController.getGuru().setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
      ScienceEngine.pin(probeConfig.getBody(), true);
      ScienceEngine.selectParameter(probeConfig.getBody(), probeConfig.getParameter(), 
          probeConfig.getLow(), (IScience2DView) getStage());
    }
    ScienceEngine.selectBody(null, science2DController.getView());
    image.setVisible(true);
    ScienceEngine.setProbeMode(true);
  }
  
  @Override
  public void checkProgress() {
  }
  
  @Override
  public void systemReadyToFinish(boolean success) {
    netSuccesses += success ? 1 : -1;
    if (!success) {
      guru.showWrong(getFailurePoints());
      setSuccessPoints(getFailurePoints()); // Equate isAttempted and failure scores
      // No failure exit.
      return;
    }
    guru.showCorrect(getSuccessPoints());
    super.systemReadyToFinish(true);
  }

  public void initialize(IModelConfig<?> probeConfig, 
      String resultExprString, String resultType) {
    this.probeConfig = probeConfig;

    this.resultType = ResultType.valueOf(resultType);
    IDoneCallback doneCallback = new IDoneCallback() {
      @Override public void done(boolean success) {
        systemReadyToFinish(success);
      }
    };
    if (this.resultType == ResultType.Spin) {   
      image.setX(ScreenComponent.VIEWPORT_WIDTH / 2 - image.getWidth() / 2 - ScreenComponent.getScaledX(50));
      image.setY(ScreenComponent.VIEWPORT_HEIGHT / 2 - image.getHeight() / 2);
      
      Image clockwise = createResultImage(ScienceEngine.getTextureRegion("clockwise"), 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      Image dontCare = createResultImage(ScienceEngine.getTextureRegion("cross"), 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      Image antiClockwise = createResultImage(ScienceEngine.getTextureRegion("anticlockwise"), 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      imageListener = new ClickResult(doneCallback, new Image[] {clockwise, antiClockwise, dontCare},
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
      image.addListener(imageListener);   

    } else {
      image.setX(ScreenComponent.getScaledX(700) - image.getWidth()/2);
      image.setY(ScreenComponent.getScaledY(250) - image.getHeight()/2);
      //Actor actor = science2DController.getModelControls().findActor(probeConfig.getName());
      //image.setPosition(actor.getX(), actor.getY());
     
      AtlasRegion arrow = ScienceEngine.getTextureRegion("fieldarrow");
      final Image decrease = createResultImage(new TextureRegion(arrow, arrow.getRegionX() + arrow.getRegionWidth(), 
          arrow.getRegionY(), -arrow.getRegionWidth(), arrow.getRegionHeight()), 2);
      final Image dontCare = createResultImage(ScienceEngine.getTextureRegion("cross"), 1);
      final Image increase = createResultImage(arrow, 2);

      switch (this.resultType) {
        case None: imageListener.setResult(2); break;
        case Direct: imageListener.setResult(1); break;
        case Inverse: imageListener.setResult(0); break;
      }
      Table list = new Table(guru.getSkin());
      list.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          if (event.getTarget() == increase) {
            systemReadyToFinish(ParameterProber.this.resultType == ResultType.Direct);
          } else if (event.getTarget() == decrease) {
            systemReadyToFinish(ParameterProber.this.resultType == ResultType.Inverse);
          } else if (event.getTarget() == dontCare) {
            systemReadyToFinish(ParameterProber.this.resultType == ResultType.None);
          }
        }
      });
      list.setPosition(image.getX() - 100, image.getY());
      list.add("Decrease"); list.add(decrease).width(50).height(50).right(); list.row();
      decrease.addAction(Actions.repeat(-1, 
          Actions.sequence(
              Actions.sizeTo(0, 50),
              Actions.sizeTo(50, 50, 2))));
      list.add("No Effect"); list.add(dontCare).width(50).height(50).center(); list.row();
      list.add("Increase"); list.add(increase).width(50).height(50).left(); list.row();
      increase.addAction(Actions.repeat(-1, 
          Actions.sequence(
              Actions.sizeTo(100, 50, 2),
              Actions.sizeTo(50, 50, 2))));
      this.addActor(list);
    }
    this.addActor(image);

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