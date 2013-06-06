package com.mazalearn.scienceengine.app.screens;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.tutor.ImageMessageBox;

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
    public void showHelp(Stage stage, boolean animate);
  }
  
  public static class ClickHandler {
    private final ImageMessageBox contentBox;
    private String content;
    int currentComponent = 0;
    private Image arrow;
    private List<IHelpComponent> helpComponents;
    private IHelpComponent helpComponent;
    private Stage stage;

    public ClickHandler(Stage stage, List<IHelpComponent> iHelpComponents,
        ImageMessageBox contentBox, String content, Image arrow) {
      this.stage = stage;
      this.helpComponents = iHelpComponents;
      this.contentBox = contentBox;
      this.content = content;
      this.arrow = arrow;
      this.currentComponent = helpComponents.size();
      
      arrow.setPosition(CENTER_POS.x, CENTER_POS.y);
      setContent(content, CENTER_POS.x, CENTER_POS.y, 0);
      arrow.setVisible(false);
      contentBox.getPrevButton().setVisible(false);
    }

    private void setContent(String text, float arrowX, float arrowY, float angle) {
      contentBox.setTextAndResize(text, false);
      // Put contentbutton on screen touching arrow based on quadrant
      int sx = (int) Math.signum(MathUtils.cosDeg(angle));
      int sy = (int) Math.signum(MathUtils.sinDeg(angle));
      float w = contentBox.getWidth();
      float h = contentBox.getHeight();
      if (arrowX == CENTER_POS.x && arrowY == CENTER_POS.y) {
        pos.set(CENTER_POS.x - w / 2, CENTER_POS.y - h / 2);
      } else if (sx >= 0 && sy >= 0) {
        pos.set(arrowX - w, arrowY - h);
      } else if (sx <= 0 && sy >= 0) {
        pos.set(arrowX, arrowY - h);
      } else if (sx <= 0 && sy <= 0) {
        pos.set(arrowX + 10, arrowY + 10);
      } else {
        pos.set(arrowX - w, arrowY);
      }
      // Ensure contentBox is entirely within screen
      if (pos.x < 0) pos.x = 0;
      if (pos.x + w > ScreenComponent.VIEWPORT_WIDTH) pos.x = ScreenComponent.VIEWPORT_WIDTH - w;
      if (pos.y < 0) pos.y = 0;
      if (pos.y + h > ScreenComponent.VIEWPORT_HEIGHT) pos.y = ScreenComponent.VIEWPORT_HEIGHT - h;
      contentBox.setPosition(pos.x, pos.y);
    }
    
    private void endHelp() {
      if (helpComponent != null) {
        helpComponent.showHelp(stage, false);
      }      
    }
    
    private void showHelp(final int increment) {
      contentBox.addAction(Actions.sequence(
          Actions.alpha(0.2f, 1),
          new Action() {
            @Override public boolean act(float delta) {
              String text;
              currentComponent = (currentComponent + increment + helpComponents.size() + 1) % (helpComponents.size() + 1);
              if (helpComponent != null) {
                helpComponent.showHelp(stage, false);
              }
              if (currentComponent == helpComponents.size() ){
                arrow.setPosition(CENTER_POS.x, CENTER_POS.y);
                setContent(content, CENTER_POS.x, CENTER_POS.y, 0);
                arrow.setVisible(false);
                currentComponent = 0;
                contentBox.getPrevButton().setVisible(false);
                helpComponent = null;
              } else {
                helpComponent = helpComponents.get(currentComponent);
                contentBox.getPrevButton().setVisible(true);
                text = helpComponent.getLocalizedName() + "\n" +
                    getMsg("Help." + helpComponent.getComponentType()) + "\n\n\n";                
                float angle = repositionArrow(stage, helpComponent, arrow);
                arrow.setVisible(true);
                setContent(text, arrow.getX(), arrow.getY(), angle);
              }
              return true;
            }
          },
          Actions.alpha(1, 1)));
    }
  }

  private static float repositionArrow(Stage stage, IHelpComponent helpComponent, Actor arrow) {
    pos.set(helpComponent.getX() + helpComponent.getWidth() / 2, 
        helpComponent.getY() + helpComponent.getHeight() / 2).sub(CENTER_POS);
    float angle = pos.angle();
    pos.x = helpComponent.getX() + helpComponent.getWidth() / 2 - arrow.getWidth() * MathUtils.cosDeg(angle) * 1.5f;
    pos.y = helpComponent.getY() + helpComponent.getHeight() / 2 - arrow.getHeight() * MathUtils.sinDeg(angle) * 4;
    arrow.setRotation(angle);
    arrow.setPosition(pos.x, pos.y);
    helpComponent.showHelp(stage, true);
    return angle;
  }

  private static String getMsg(String msgId) {
    return ScienceEngine.getMsg().getString(msgId);
  }

  public HelpTour(final Stage stage, Skin skin, String contents, List<IHelpComponent> iHelpComponents) {
    setPosition(0, 0);
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    this.setName(ScreenComponent.HELP_TOUR);
    // Add below Basic Screen
    Actor coreGroup = stage.getRoot().findActor(ScreenComponent.CORE_GROUP);
    stage.getRoot().addActorBefore(coreGroup, this);
    
    Image arrow = new Image(ScienceEngine.getTextureRegion("helparrow"));
    arrow.setSize(arrow.getWidth() * 1.5f, arrow.getHeight() * 1.5f);
    addActor(arrow);
    
    contents = contents + "\n\n\n\n";
    final ImageMessageBox contentBox = new ImageMessageBox(skin, "helpcloud", this);
    addActor(contentBox);

    final ClickHandler onClickHandler = new ClickHandler(getStage(), iHelpComponents, contentBox, contents, arrow);
    contentBox.getNextButton().addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        onClickHandler.showHelp(+1);
      }
    });
    contentBox.getPrevButton().addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        onClickHandler.showHelp(-1);
      }
    });
    
    contentBox.getCloseButton().addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        onClickHandler.endHelp();
      }
    });
  }
}