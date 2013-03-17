package com.mazalearn.scienceengine.app.screens;

import java.util.List;

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
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;

public class HelpTour extends Group {
  
  public interface IHelpComponent {
    public String getName();
    public float getX();
    public float getY();
    public float getWidth();
    public float getHeight();
  }
  
  public static class NextOnClick extends ClickListener {
    protected static final int SCALE = 40;
    private final TextButton contentButton;
    private String content;
    int currentComponent = 0;
    private Image arrow;
    private Vector2 pos = new Vector2();
    private Image closeImage;
    private TextButton nextButton;
    private List<IHelpComponent> helpComponents;
    private static final Vector2 CENTER_POS = 
        new Vector2(ScreenComponent.VIEWPORT_WIDTH / 2, ScreenComponent.VIEWPORT_HEIGHT / 2);

    public NextOnClick(List<IHelpComponent> iHelpComponents,
        TextButton contentButton, String content, Image arrow, TextButton nextButton, Image closeImage) {
      this.helpComponents = iHelpComponents;
      this.contentButton = contentButton;
      this.content = content;
      this.arrow = arrow;
      this.closeImage = closeImage;
      this.nextButton = nextButton;
      arrow.setPosition(CENTER_POS.x, CENTER_POS.y);
      setContent(content, CENTER_POS.x, CENTER_POS.y, 0);
      arrow.setVisible(false);
    }

    private void setContent(String text, float arrowX, float arrowY, float angle) {
      contentButton.setText(text);
      // Set size in a 3:1 aspect ratio
      float semiPerimeter = (float) Math.sqrt(text.length());
      float h = semiPerimeter * SCALE / 3 + 30; // To hold Buttons
      float w = semiPerimeter * SCALE * 3 / 4;
      ScreenComponent.scaleSize(contentButton, w, h);
      contentButton.setSize(w, h);
      contentButton.getLabel().setAlignment(Align.center, Align.left);
      // Put contentbutton on screen touching arrow based on quadrant
      int sx = (int) Math.signum(MathUtils.cosDeg(angle));
      int sy = (int) Math.signum(MathUtils.sinDeg(angle));
      if (arrowX == CENTER_POS.x && arrowY == CENTER_POS.y) {
        contentButton.setPosition(CENTER_POS.x - w / 2, CENTER_POS.y - h / 2);
      } else if (sx >= 0 && sy >= 0) {
        contentButton.setPosition(arrowX - w, arrowY - h);
      } else if (sx <= 0 && sy >= 0) {
        contentButton.setPosition(arrowX, arrowY - h);
      } else if (sx <= 0 && sy <= 0) {
        contentButton.setPosition(arrowX + 10, arrowY + 10);
      } else {
        contentButton.setPosition(arrowX - w, arrowY);
      }
      closeImage.setPosition(contentButton.getWidth() - closeImage.getWidth(), 
          contentButton.getHeight() - closeImage.getHeight());
      nextButton.setPosition(contentButton.getWidth() / 2 - nextButton.getWidth() / 2,
          5);
    }            
    
    @Override 
    public void clicked (InputEvent event, float x, float y) {
      contentButton.addAction(Actions.sequence(
          Actions.alpha(0.2f, 1),
          new Action() {
            @Override public boolean act(float delta) {
              String text;
              if (currentComponent >= helpComponents.size()){
                arrow.setPosition(CENTER_POS.x, CENTER_POS.y);
                setContent(content, CENTER_POS.x, CENTER_POS.y, 0);
                arrow.setVisible(false);
                currentComponent = 0;
              } else {
                IHelpComponent helpComponent = helpComponents.get(currentComponent++);
                text = ScienceEngine.getMsg().getString("Name." + helpComponent.getName()) + "\n" +
                    ScienceEngine.getMsg().getString("Help." + helpComponent.getName()) + "\n\n\n";
                pos.set(helpComponent.getX() + helpComponent.getWidth() / 2, 
                    helpComponent.getY() + helpComponent.getHeight() / 2).sub(CENTER_POS);
                arrow.setVisible(true);
                arrow.setRotation(pos.angle());
                arrow.setPosition(helpComponent.getX() + helpComponent.getWidth() / 2 - arrow.getWidth() * MathUtils.cosDeg(pos.angle()) * 2,
                    helpComponent.getY() + helpComponent.getHeight() / 2 - arrow.getHeight() * MathUtils.sinDeg(pos.angle()) * 2);
                setContent(text, arrow.getX(), arrow.getY(), pos.angle());
              }
              return true;
            }
          },
          Actions.alpha(1, 1)));
    }
  }

  public HelpTour(final Stage stage, Skin skin, String contents, List<IHelpComponent> iHelpComponents) {
    setPosition(0, 0);
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    // Add below Basic Screen
    Actor coreGroup = stage.getRoot().findActor(ScreenComponent.CORE_GROUP);
    stage.getRoot().addActorBefore(coreGroup, this);
    
    Image arrow = new Image(ScienceEngine.getTextureRegion("fieldarrow-yellow"));
    arrow.setSize(arrow.getWidth() * 1.5f, arrow.getHeight() * 1.5f);
    addActor(arrow);
    
    Image closeImage = new Image(ScienceEngine.getTextureRegion("close"));
    ScreenComponent.scaleSize(closeImage, closeImage.getWidth() * 0.5f, closeImage.getHeight() * 0.5f);
    closeImage.addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        stage.getRoot().removeActor(HelpTour.this);
      }      
    });
    contents = contents + "\n\n\n\n";
    final TextButton contentButton = new TextButton(contents, skin);
    contentButton.getLabel().setWrap(true);
    contentButton.addListener(new DragListener() {
      public void touchDragged (InputEvent event, float x, float y, int pointer) {
        super.touchDragged(event, x, y, pointer);
        contentButton.setPosition(contentButton.getX() + getDeltaX(), contentButton.getY() + getDeltaY());
      }
    });
    addActor(contentButton);

    TextButton nextButton = new TextButton(ScienceEngine.getMsg().getString("Name.Next"), skin);
    nextButton.setPosition(5, 5);
    nextButton.addListener(new NextOnClick(iHelpComponents, contentButton, contents, arrow, nextButton, closeImage));
    
    contentButton.addActor(closeImage);
    contentButton.addActor(nextButton);
  }
}