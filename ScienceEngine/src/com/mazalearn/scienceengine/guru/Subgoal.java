package com.mazalearn.scienceengine.guru;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.app.services.AggregatorFunction;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.IFunction;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;

public class Subgoal extends AbstractTutor {
  private final Expr postCondition;
  private Collection<Variable> variables;
  private String when;
  private boolean progress;
  private boolean isUserNext;
  
  public Subgoal(IScience2DController science2DController,
      String goal, Array<?> components, Array<?> configs,
      String when, String postConditionString,
      int deltaSuccessScore) {
    super(science2DController, goal, components, configs, deltaSuccessScore, 0);
    Parser parser = createParser();
    try {
      this.postCondition = parser.parseString(postConditionString);
    } catch (SyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.variables = parser.getVariables();
    // We use presence of dummy variable UserNext as argument of function UserNext
    // to indicate that user input of next is expected to succeed in this subgoal.
    for (Variable v: variables) {
      String name = v.name();
      if (name.equals("NextButton")) {
        isUserNext = false;
        break;
      }
    }
    
    // Create a button NEXT at right place along with listener to set isUserNext.
    if (!isUserNext) {
      // TODO: Move next button to dashboard next to Hint button
      final Button next = new TextButton("Next", science2DController.getSkin());
      next.setColor(Color.YELLOW);
      next.addListener(new ClickListener() {
        public void clicked (InputEvent event, float x, float y) {
          next.setVisible(false);
          isUserNext = true;
        }      
      });
      next.setPosition(570, -510);
      addActor(next);
    }
    
    this.when = when;
  }

  private Parser createParser() {
    Parser parser = new Parser();
    Map<String, IFunction.A0> functions0 = new HashMap<String, IFunction.A0>();
    Map<String, IFunction.A1> functions1 = new HashMap<String, IFunction.A1>();
    Map<String, IFunction.A2> functions2 = new HashMap<String, IFunction.A2>();
    for (AggregatorFunction aggregatorFunction: AggregatorFunction.values()) {
      functions1.put(aggregatorFunction.name(), aggregatorFunction);
    }
    functions1.put("UserInput", new IFunction.A1() {
      @Override
      public float eval(String parameter) {
        return isUserNext ? 1 : 0;
      }     
    });
    parser.allowFunctions(functions0, functions1, functions2);
    return parser;
  }

  public String getWhen() {
    return when;
  }

  public boolean hasSucceeded() {
    if (postCondition == null) return false;  
    science2DController.getModel().bindParameterValues(variables);
    return postCondition.bvalue();
  }


  @Override
  public boolean hasFailed() {
    return false; // Allow learner to keep trying forever
  }

  public void checkProgress() {
    this.progress = hasSucceeded();
  }

  @Override
  public String getGoal() {
    if (!progress) {
      return super.getGoal();
    }
    return null;
  }

  @Override
  public void activate(boolean activate) {
    this.setVisible(activate);
  }
}