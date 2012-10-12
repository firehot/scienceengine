package com.mazalearn.scienceengine.core.probe;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ProbeHinter extends Group {
  private static List<Image> SCIENTISTS = new ArrayList<Image>();
  private TextButton hintButton;
  float increment = 1, count = 0;
  private int scientistIndex = 0;
  private boolean jumpingMode = false;

  static {
    SCIENTISTS.add(new Image(new Texture("images/edison.png")));
    SCIENTISTS.get(0).setName("Thomas Alva Edison");
    SCIENTISTS.add(new Image(new Texture("images/oersted.png")));
    SCIENTISTS.get(1).setName("Hans Christian Oersted");
    SCIENTISTS.add(new Image(new Texture("images/faraday.png")));
    SCIENTISTS.get(2).setName("Michael Faraday");
  }
  public ProbeHinter(Skin skin) {
    
    hintButton = new TextButton("", skin);
    hintButton.setColor(Color.YELLOW);
    hintButton.getLabel().setWrap(true);
    
    hintButton.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        hintButton.setVisible(false);
        SCIENTISTS.get(scientistIndex).setVisible(false);      }
    });
    
    for (final Image image: SCIENTISTS) {
      image.setSize(42, 42);
      this.addActor(image);
      image.setVisible(false);
      image.addListener(new ClickListener() {
        public void clicked (InputEvent event, float x, float y) {
          jumpingMode = false;
          hintButton.setVisible(true);
          hintButton.setPosition(image.getX() - hintButton.getWidth(), image.getY());
        }
      });    
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
    this.setVisible(true);
    hintButton.setText("Hint: " + hint + "\n-" + SCIENTISTS.get(scientistIndex).getName());
    // TODO: BUG in libgdx for wrapped labels ??? hence setting height
    hintButton.setSize(300, 100); 
    scientistIndex = MathUtils.random(0, SCIENTISTS.size() - 1);
    jumpingMode = true;
    SCIENTISTS.get(scientistIndex).setVisible(true);
  }

}
