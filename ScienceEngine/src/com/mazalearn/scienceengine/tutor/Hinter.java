package com.mazalearn.scienceengine.tutor;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;

public class Hinter extends Group {
  private static List<Image> SCIENTISTS = new ArrayList<Image>();
  float increment = 1, count = 0;
  private int scientistIndex = 0;
  private boolean jumpingMode = false;
  private String hint;
  
  static {
    SCIENTISTS.add(new Image());
  }
/*
  static {
    Image image;
    SCIENTISTS.add(image = new Image(ScienceEngine.getTextureRegion("edison")));
    image.setName("Thomas Alva Edison");
    SCIENTISTS.add(image = new Image(ScienceEngine.getTextureRegion("oersted.png")));
    image.setName("Hans Christian Oersted");
    SCIENTISTS.add(image = new Image(ScienceEngine.getTextureRegion("faraday.png")));
    image.setName("Michael Faraday");
  } */
  
  public Hinter(Skin skin) {
    
    ClickListener imageClickListener = new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        if (jumpingMode) {
          jumpingMode = false;
        } else {
          SCIENTISTS.get(scientistIndex).setVisible(false);
          Hinter.this.addAction(Actions.delay(20, new Action() {
            @Override
            public boolean act(float delta) {
              clearHint();
              return true;
            }          
          }));
        }
      }
    };
    for (final Image image: SCIENTISTS) {
      image.setSize(42, 42);
      this.addActor(image);
      image.setVisible(false);
      image.addListener(imageClickListener);
    }    
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (!jumpingMode) return;
    // Jump up and down
    if (count++ % 20 == 0) {
      increment = -increment;
    }
    Image image = SCIENTISTS.get(scientistIndex);
    image.setY(image.getY() + increment);
  }
  
  public void setHint(String hint) {
    if (hint == this.hint) return;
    if (hint == null) {
      this.setVisible(false);
      return;
    }
    this.hint = hint;
    this.setVisible(true);
    Image image = SCIENTISTS.get(scientistIndex);
    image.setVisible(false);
    scientistIndex = MathUtils.random(0, SCIENTISTS.size() - 1);
    jumpingMode = false; // TODO: very irritating
    image = SCIENTISTS.get(scientistIndex);
    ScienceEngine.displayStatusMessage(getStage(), "Hint: " + hint);
    image.setY(0);
  }

  public boolean hasHint() {
    return hint != null;
  }
  
  public void clearHint() {
    this.hint = null;
    ScienceEngine.displayStatusMessage(getStage(), "");
  }

}
