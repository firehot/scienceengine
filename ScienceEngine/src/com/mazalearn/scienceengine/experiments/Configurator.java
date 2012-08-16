package com.mazalearn.scienceengine.experiments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;
import com.mazalearn.scienceengine.ScienceEngine;

public class Configurator extends Table {
  public interface Condition {
    public boolean eval();
  };
  
  static class Config {
    final Cell[] cells;
    Condition condition = null;

    public Config(Cell[] cells) {
      this.cells = cells;
    }

    public void addCondition(Condition condition) {
      this.condition = condition;
    }
    
    void validate() {
      boolean condValue = condition == null || condition.eval();
      for (Cell cell: cells) {
        if (condValue) cell.size(null); else cell.size(0);
      }
    }
  }
  /**
   * Utility class for experiment floating point sliders using reflection.
   *
   */
  static class ConfigSlider extends Slider {
    final Experiment experiment;
    final String property;
    final Method getter, setter;
    
    public ConfigSlider(Experiment experiment, String property, 
        float low, float high, Skin skin) {
      super(low, high, (high - low)/10, skin);
      this.experiment = experiment;
      this.property = property;
      // Find getter and setter for property by reflection
      try {
        getter = experiment.getClass().getMethod("get" + property);
        setter = experiment.getClass().getMethod(
            "set" + property, new Class[] {float.class});
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Could not find getter or setter");
      }
      // Initialize slider value
      setValue(getVal());
      // Set value when slider changes
      setValueChangedListener(new ValueChangedListener() {
        @Override
        public void changed(Slider slider, float value) {
          setVal(value);
        }      
      });
    }
    
    float getVal() {
      try {
        return (Float) getter.invoke(experiment);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return 0;
    }
    
    void setVal(float value) {
      try {
        setter.invoke(experiment, value);
        Gdx.app.log(ScienceEngine.LOG, "Setting " + property + " to " + value);
      } catch (Exception e) {
        e.printStackTrace();
        Gdx.app.log(ScienceEngine.LOG, "Failed to set " + property + " to " + value);
      }     
    }
  }
  
  /**
   * Utility class for experiment action buttons using reflection.
   *
   */
  static class ConfigTextButton extends TextButton {
    public ConfigTextButton(final Experiment experiment, final String action, 
        final Skin skin) {
      super(action, skin);
      // Find method for action by reflection and set onclick handler
      final Method actionMethod;
      try {
        actionMethod = experiment.getClass().getMethod(action.toLowerCase());
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Could not find action method");
      }
      this.setClickListener(new ClickListener() {
        @Override
        public void click(Actor actor, float x, float y) {
          try {
            actionMethod.invoke(experiment);
          } catch (Exception e) {
            throw new RuntimeException("Could not invoke action " + action);
          }
        }
      });
    }
  }
  
  static class ConfigSelectBox extends SelectBox {
    final Experiment experiment;
    final String property;
    final Method getter, setter;

    public ConfigSelectBox(final Experiment experiment, final String property, 
        final String[] items, final Skin skin) {
      super(items, skin);
      this.experiment = experiment;
      this.property = property;
      // Find getter and setter for property by reflection
      try {
        getter = experiment.getClass().getMethod("get" + property);
        setter = experiment.getClass().getMethod(
            "set" + property, new Class[] {String.class});
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Could not find getter or setter");
      }
      // Initialize selected item
      setSelection(getVal());
      // Set value when slider changes
      setSelectionListener(new SelectionListener() {
        @Override
        public void selected(Actor actor, int index, String value) {
          experiment.reset();
          setVal(value);
        }      
      });
    }
    
    String getVal() {
      try {
        return (String) getter.invoke(experiment);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return "";
    }
    
    void setVal(String value) {
      try {
        setter.invoke(experiment, value);
        Gdx.app.log(ScienceEngine.LOG, "Setting " + property + " to " + value);
      } catch (Exception e) {
        e.printStackTrace();
        Gdx.app.log(ScienceEngine.LOG, "Failed to set " + property + " to " + value);
      }     
    }
  }
  
  final Experiment experiment;
  final Skin skin;
  List<Config> configs;
  
  // use cell ignore to hide unwanted cells?
  // pause and resume should be exclusive
  // want condition on an added config such that it is visible only on condition true
  // means it has to override draw and reevaluate conditions for each "added" config.
  public Configurator(Skin skin, final Experiment experiment) {
    super(skin);
    this.skin = skin;
    this.experiment = experiment;
    this.configs = new ArrayList<Config>();
    addButton("Pause").addCondition(new Condition() {
      public boolean eval() {
        return !experiment.isPaused();
      }
    });
    addButton("Resume").addCondition(new Condition() {
      public boolean eval() {
        return experiment.isPaused();
      }
    });
    addButton("Reset");
  }
  
  public Config addButton(String caption) {
    Cell[] cells = new Cell[] {
      add(new ConfigTextButton(experiment, caption, skin)),
      row()
    };
    Config config = new Config(cells);
    this.configs.add(config);
    return config;
  }
  
  public Config addSlider(String property, float low, float high) {
    Cell[] cells = new Cell[] {
      add(property),
      row(),
      add(new ConfigSlider(experiment, property, low, high, skin)),
      row()
    };
    Config config = new Config(cells);
    this.configs.add(config);
    return config;
  }

  public Config addSelect(String property, String[] items) {
    Cell[] cells = new Cell[] {
      add(new ConfigSelectBox(experiment, property, items, skin)),
      row()
    };
    Config config = new Config(cells);
    this.configs.add(config);
    return config;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    for (Config config: configs) {
      config.validate();
    }
    super.draw(batch, parentAlpha);
  }
}