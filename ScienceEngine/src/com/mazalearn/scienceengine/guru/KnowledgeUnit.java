package com.mazalearn.scienceengine.guru;

import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;

public class KnowledgeUnit extends AbstractTutor {
  private Expr postCondition;
  private Collection<Variable> variables;
  private String when;
  private boolean isUserNext = false;
  private Button nextButton;
  private boolean postConditionSatisfied;
  private String[] plaudits = {"Bravo", "Well Done", "Excellent", "Good"};
  
  public KnowledgeUnit(IScience2DController science2DController,
      ITutor parent, String goal, String name, Array<?> components, Array<?> configs,
      int deltaSuccessScore, String[] hints) {
    super(science2DController, parent, goal, name, components, configs, deltaSuccessScore, 0, hints);
    
    // Create a button NEXT at right place along with listener to set isUserNext.
    nextButton = new TextButton("Next", science2DController.getSkin(), "body");
    nextButton.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        nextButton.setVisible(false);
        isUserNext = true;
      }
    });
    nextButton.setPosition(ScreenComponent.NextButton.getX(nextButton.getWidth()),
        ScreenComponent.NextButton.getY(nextButton.getHeight()));
    addActor(nextButton);    
  }

  public void initialize(String when, String postConditionString) {
    Parser parser = guru.createParser();
    try {
      this.postCondition = parser.parseString(postConditionString);
    } catch (SyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.variables = parser.getVariables();
    this.when = when;
  }

  public String getWhen() {
    return when;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (!isVisible() || Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    checkProgress();
  }

  @Override
  public void reset() {
    super.reset();
    isUserNext = false;
    nextButton.setVisible(false);
  }
  
  @Override
  public void checkProgress() {
    if (postCondition == null) return;
    if (!postConditionSatisfied) {
      science2DController.getModel().bindParameterValues(variables);
      postConditionSatisfied = postCondition.bvalue();
      if (postConditionSatisfied) {
        nextButton.setVisible(true);
        String plaudit = plaudits[MathUtils.random(0, plaudits.length - 1)];
        guru.showSuccess(plaudit);
        Gdx.app.log(ScienceEngine.LOG, "Subgoal satisfied: " + getGoal());
      }
    }
    if (isUserNext && postConditionSatisfied) {
      done(true);
    }
  }
}