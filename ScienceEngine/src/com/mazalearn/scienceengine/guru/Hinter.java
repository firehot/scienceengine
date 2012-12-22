package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Hinter extends Group {
  private static List<Image> SCIENTISTS = new ArrayList<Image>();
  private TextButton hintButton;
  float increment = 1, count = 0;
  private int scientistIndex = 0;
  private boolean jumpingMode = false;
  private String hint;

  static {
    Image image;
    SCIENTISTS.add(image = new Image(new Texture("images/edison.png")));
    image.setName("Thomas Alva Edison");
    SCIENTISTS.add(image = new Image(new Texture("images/oersted.png")));
    image.setName("Hans Christian Oersted");
    SCIENTISTS.add(image = new Image(new Texture("images/faraday.png")));
    image.setName("Michael Faraday");
  }
  public Hinter(Skin skin) {
    
    hintButton = new TextButton("", skin);
    hintButton.setColor(Color.YELLOW);
    hintButton.getLabel().setWrap(true);
    
    final ClickListener buttonClickListener = new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        hintButton.setVisible(false);
        SCIENTISTS.get(scientistIndex).setVisible(false);
        Hinter.this.addAction(Actions.delay(20, new Action() {
          @Override
          public boolean act(float delta) {
            clearHint();
            return true;
          }          
        }));
      }
    };
    hintButton.addListener(buttonClickListener);
    
    ClickListener imageClickListener = new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        if (jumpingMode) {
          jumpingMode = false;
          hintButton.setVisible(true);
          hintButton.setPosition(-hintButton.getWidth(), -50);
        } else {
          buttonClickListener.clicked(null, 0, 0);
        }
      }
    };
    for (final Image image: SCIENTISTS) {
      image.setSize(42, 42);
      this.addActor(image);
      image.setVisible(false);
      image.addListener(imageClickListener);    
    }
    
    this.addActor(hintButton);
    hintButton.setVisible(false);
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
    if (hint == null) {
      this.setVisible(false);
      return;
    }
    if (hint == this.hint) return;
    this.hint = hint;
    this.setVisible(true);
    Image image = SCIENTISTS.get(scientistIndex);
    image.setVisible(false);
    // TODO: BUG in libgdx for wrapped labels ??? hence setting height
    hintButton.setSize(300, 100); 
    scientistIndex = MathUtils.random(0, SCIENTISTS.size() - 1);
    jumpingMode = true;
    image = SCIENTISTS.get(scientistIndex);
    hintButton.setText("Hint: " + hint + "\n-" + image.getName());
    image.setVisible(true);
    image.setY(0);
    hintButton.setVisible(true);
    hintButton.setPosition(-hintButton.getWidth(), -50);
  }

  public boolean hasHint() {
    return hint != null;
  }
  
  public void clearHint() {
    this.hint = null;
  }

}
