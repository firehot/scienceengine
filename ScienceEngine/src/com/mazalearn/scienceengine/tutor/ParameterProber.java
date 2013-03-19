package com.mazalearn.scienceengine.tutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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
import com.mazalearn.scienceengine.core.view.SizeAction;

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
  private Vector2 coords = new Vector2();
  private Table changeChoices;
  private Image delta;
  
  private Image createResultImage(TextureRegion textureRegion, float x, float y) {
    Image image = new Image(textureRegion);
    image.setVisible(false);
    ScreenComponent.scaleSize(image, image.getWidth(), image.getHeight());
    image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
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
    ScienceEngine.setProbeMode(true);
    ScienceEngine.pin(probeConfig.getBody(), true);
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
      float middle = (probeConfig.getLow() + probeConfig.getHigh()) / 2;
      ((IModelConfig<Float>)probeConfig).setValue(middle);
      ScienceEngine.selectParameter(probeConfig.getBody(), probeConfig.getParameter(), 
          middle, (IScience2DView) getStage());
      Table actor = (Table) science2DController.getModelControls().findActor(probeConfig.getName());
      coords.set(actor.getPrefWidth() / 2 - image.getWidth() / 2, 
          actor.getPrefHeight() / 2 - image.getHeight() / 2);
      actor.localToStageCoordinates(coords);
      image.setPosition(coords.x, coords.y);
      delta.setPosition(coords.x + image.getWidth(), coords.y + delta.getHeight() / 2);
      changeChoices.setPosition(image.getX() - 140, image.getY() + 30);
      // TODO: Not sure why below line is required - without it, modelcontrols disappears.
      ScienceEngine.pin(probeConfig.getBody(), true);
      science2DController.getGuru().setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
    }
    ScienceEngine.selectBody(null, science2DController.getView());
    image.setVisible(true);
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
      Image antiClockwise = createResultImage(ScienceEngine.getTextureRegion("anticlockwise"), 
          image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
      imageListener = new ClickResult(doneCallback, new Image[] {clockwise, antiClockwise},
          new ClickResult.StateMapper() {
        @Override
        public int map(float x, float y) {
          return (x < image.getWidth() / 2) ? 0 : 1;
         }
      });
      this.addActor(clockwise);
      this.addActor(antiClockwise);
      image.addListener(imageListener);   

    } else {
     
      AtlasRegion arrow = ScienceEngine.getTextureRegion("fieldarrow");
      final Image decrease = new Image(new TextureRegion(arrow, arrow.getRegionX() + arrow.getRegionWidth(), 
          arrow.getRegionY(), -arrow.getRegionWidth(), arrow.getRegionHeight()));
      final Image dontCare = new Image(ScienceEngine.getTextureRegion("cross"));
      final Image increase = new Image(arrow);
      delta = new Image(ScienceEngine.getTextureRegion("fieldarrow-yellow"));

      changeChoices = new Table(guru.getSkin());
      changeChoices.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          boolean success = false;
          if (event.getTarget() == increase) {
            success = ParameterProber.this.resultType == ResultType.Direct;
            systemReadyToFinish(success);
          } else if (event.getTarget() == decrease) {
            success = ParameterProber.this.resultType == ResultType.Inverse;
            systemReadyToFinish(success);
          } else if (event.getTarget() == dontCare) {
            success = ParameterProber.this.resultType == ResultType.None;
            systemReadyToFinish(success);
          }
          changeChoices.setVisible(!success);
        }
      });
      changeChoices.add("Decreases"); changeChoices.add(decrease).width(50).height(50).right(); changeChoices.row();
      changeChoices.add("Is Unaffected"); changeChoices.add(dontCare).width(50).height(50).center(); changeChoices.row();
      changeChoices.add("Increases"); changeChoices.add(increase).width(50).height(50).left(); changeChoices.row();
      delta.addAction( 
          Actions.forever(
          Actions.sequence(
             SizeAction.sizeTo(0, 30, 0),
             SizeAction.sizeTo(50, 30, 2)
              )));
      this.addActor(changeChoices);
      this.addActor(delta);
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