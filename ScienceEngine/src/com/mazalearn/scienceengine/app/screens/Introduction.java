package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;

public class Introduction extends Group {
  
  public static class ExpandOnClick extends ClickListener {
    protected static final int SCALE = 33;
    private final TextButton contentButton;
    private String content;
    int currentComponent = 0;
    private Image arrow;
    private Vector2 pos = new Vector2();
    private static final Vector2 CENTER_POS = 
        new Vector2(AbstractScreen.VIEWPORT_WIDTH / 2, AbstractScreen.VIEWPORT_HEIGHT / 2);

    public ExpandOnClick(TextButton contentButton, String content, Image arrow) {
      this.contentButton = contentButton;
      this.content = content;
      this.arrow = arrow;
      arrow.setPosition(CENTER_POS.x, CENTER_POS.y);
      setContent(content, CENTER_POS.x + 300, CENTER_POS.y + 200, 0);
      arrow.setVisible(false);
    }

    private void setContent(String text, float arrowX, float arrowY, float angle) {
      contentButton.setText(text);
      // Set size in a 3:1 aspect ratio
      float semiPerimeter = (float) Math.sqrt(text.length());
      float h = semiPerimeter * SCALE / 3 + 50; // To hold OK Button
      float w = semiPerimeter * SCALE * 3 / 4;
      contentButton.setSize(w, h);
      // Put contentbutton on screen touching arrow based on quadrant
      int sx = (int) Math.signum(MathUtils.cosDeg(angle));
      int sy = (int) Math.signum(MathUtils.sinDeg(angle));
      if (sx >= 0 && sy >= 0) {
        contentButton.setPosition(arrowX - w, arrowY - h);
      } else if (sx <= 0 && sy >= 0) {
        contentButton.setPosition(arrowX, arrowY - h);
      } else if (sx <= 0 && sy <= 0) {
        contentButton.setPosition(arrowX + 10, arrowY + 10);
      } else {
        contentButton.setPosition(arrowX - w, arrowY);
      }
    }            
    
    @Override 
    public void clicked (InputEvent event, float x, float y) {
      contentButton.addAction(Actions.sequence(
          Actions.alpha(0.2f, 1),
          new Action() {
            @Override public boolean act(float delta) {
              String text;
              ScreenComponent[] screenComponents = ScreenComponent.values();
              while (!screenComponents[currentComponent].showInHelpTour()) {
                currentComponent++;
                if (currentComponent == screenComponents.length) break; 
              }
              if (currentComponent >= screenComponents.length){
                arrow.setPosition(CENTER_POS.x, CENTER_POS.y);
                setContent(content, CENTER_POS.x + 300, CENTER_POS.y + 200, 0);
                arrow.setVisible(false);
                currentComponent = 0;
              } else {
                ScreenComponent screenComponent = screenComponents[currentComponent++];
                text = ScienceEngine.getMsg().getString("Help." + screenComponent.name()) + "\n" +
                    ScienceEngine.getMsg().getString("Help.Continue") + "\n\n\n";
                pos.set(screenComponent.getX(), screenComponent.getY()).sub(CENTER_POS);
                arrow.setVisible(true);
                arrow.setRotation(pos.angle());
                arrow.setPosition(screenComponent.getX() - arrow.getWidth() * MathUtils.cosDeg(pos.angle()) * 2,
                    screenComponent.getY() - arrow.getHeight() * MathUtils.sinDeg(pos.angle()) * 2);
                setContent(text, arrow.getX(), arrow.getY(), pos.angle());
              }
              return true;
            }
          },
          Actions.alpha(1, 1)));
    }
  }

  public Introduction(final Stage stage, Skin skin, String title, 
      String contents, String instructions, String buttonText) {  
    setPosition(0, 0);
    setSize(AbstractScreen.VIEWPORT_WIDTH, AbstractScreen.VIEWPORT_HEIGHT);
    stage.addActor(this);
    // Move backbutton to top
    Actor backButton = stage.getRoot().findActor(ScreenComponent.Back.name());
    stage.addActor(backButton);
    
    Image arrow = new Image(new Texture("images/fieldarrow-yellow.png"));
    arrow.setSize(arrow.getWidth() * 1.5f, arrow.getHeight() * 1.5f);
    addActor(arrow);
    
    contents += "\n\n" + ScienceEngine.getMsg().getString("Help.Tour") + "\n\n";
    TextButton contentButton = new TextButton(contents, skin);
    contentButton.getLabel().setWrap(true);
    contentButton.addListener(new ExpandOnClick(contentButton, contents, arrow));
    addActor(contentButton);

    TextButton okButton = new TextButton(ScienceEngine.getMsg().getString("Name.Close"), skin);
    okButton.setPosition(5, 5);
    okButton.addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        stage.getRoot().removeActor(Introduction.this);
      }      
    });
    contentButton.addActor(okButton);
  }
}