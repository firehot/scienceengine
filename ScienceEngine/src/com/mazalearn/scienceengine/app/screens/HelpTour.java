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
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.AnimateAction;

public class HelpTour extends Group {
  
  private static final Vector2 CENTER_POS = 
      new Vector2(ScreenComponent.VIEWPORT_WIDTH / 2, ScreenComponent.VIEWPORT_HEIGHT / 2);
  private static Vector2 pos = new Vector2();

  public interface IHelpComponent {
    // Distinct localized name in the stage of this component
    public String getLocalizedName();
    // Component type of this component
    public String getComponentType();
    public float getX();
    public float getY();
    public float getWidth();
    public float getHeight();
  }
  
  public static class NextOnClick extends ClickListener {
    protected static final int SCALE = 60;
    private final TextButton contentButton;
    private String content;
    int currentComponent = 0;
    private Image arrow;
    private Image closeImage;
    private TextButton nextButton;
    private List<IHelpComponent> helpComponents;
    private IHelpComponent helpComponent;

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
      float h = semiPerimeter * SCALE / 4 + 30; // To hold Buttons
      float w = semiPerimeter * SCALE * 3 / 4 + 40;
      ScreenComponent.scaleSize(contentButton, w, h);
      contentButton.setSize(w, h);
      contentButton.getLabel().setAlignment(Align.center, Align.left);
      // Put contentbutton on screen touching arrow based on quadrant
      int sx = (int) Math.signum(MathUtils.cosDeg(angle));
      int sy = (int) Math.signum(MathUtils.sinDeg(angle));
      nextButton.setText(getMsg("HelpTour.Next"));
      if (arrowX == CENTER_POS.x && arrowY == CENTER_POS.y) {
        contentButton.setPosition(CENTER_POS.x - w / 2, CENTER_POS.y - h / 2);
        nextButton.setText(getMsg("HelpTour.Help"));
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
                helpComponent = null;
              } else {
                if (helpComponent != null && helpComponent instanceof Actor) {
                  ((Actor) helpComponent).clearActions();
                }
                helpComponent = helpComponents.get(currentComponent++);
                text = helpComponent.getLocalizedName() + "\n" +
                    getMsg("Help." + helpComponent.getComponentType()) + "\n\n\n";                
                float angle = repositionArrow(helpComponent, arrow);
                arrow.setVisible(true);
                setContent(text, arrow.getX(), arrow.getY(), angle);
              }
              return true;
            }
          },
          Actions.alpha(1, 1)));
    }
  }

  private static float repositionArrow(IHelpComponent helpComponent, Actor arrow) {
    pos.set(helpComponent.getX() + helpComponent.getWidth() / 2, 
        helpComponent.getY() + helpComponent.getHeight() / 2).sub(CENTER_POS);
    float angle = pos.angle();
    pos.x = helpComponent.getX() + helpComponent.getWidth() / 2 - arrow.getWidth() * MathUtils.cosDeg(angle) * 1.5f;
    pos.y = helpComponent.getY() + helpComponent.getHeight() / 2 - arrow.getHeight() * MathUtils.sinDeg(angle) * 4;
    arrow.setRotation(angle);
    arrow.setPosition(pos.x, pos.y);
    if (helpComponent instanceof Actor) {
      Actor a = (Actor) helpComponent;
      a.addAction(AnimateAction.animate(a.getWidth(), a.getHeight()));
    }
    return angle;
  }

  private static String getMsg(String msgId) {
    return ScienceEngine.getMsg().getString(msgId);
  }

  public HelpTour(final Stage stage, Skin skin, String contents, List<IHelpComponent> iHelpComponents) {
    setPosition(0, 0);
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    this.setName(ScreenComponent.HELP_TOUR.name());
    // Add below Basic Screen
    Actor coreGroup = stage.getRoot().findActor(ScreenComponent.CORE_GROUP);
    stage.getRoot().addActorBefore(coreGroup, this);
    
    Image arrow = new Image(ScienceEngine.getTextureRegion("helparrow"));
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
    final TextButton contentButton = ScreenUtils.createImageMessageBox(skin, "helpcloud");
    contentButton.getLabel().setWrap(true);
    addActor(contentButton);

    TextButton nextButton = new TextButton(getMsg("HelpTour.Next"), skin);
    nextButton.setPosition(5, 5);
    nextButton.addListener(new NextOnClick(iHelpComponents, contentButton, contents, arrow, nextButton, closeImage));
    
    contentButton.addActor(closeImage);
    contentButton.addActor(nextButton);
  }
}