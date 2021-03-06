package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.view.CommandClickListener;

public class ExplanationBox extends ImageMessageBox {

  int current;
  String[] explanation;
  private TextButton reviseButton;
  private boolean hasRevisionRefs;
  
  public ExplanationBox(final TutorHelper tutorHelper, Skin skin, String textureName) {
    super(skin, null, null);
    getNextButton().addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        current++;
        showExplanation();
      }            
    });
    getPrevButton().addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        current--;
        showExplanation();
      }            
    });
    Image themeImage = new Image(ScienceEngine.getTextureRegion(textureName));
    addActor(themeImage);
    themeImage.setPosition(0, 0);
    themeImage.setSize(ScreenComponent.getScaledX(30), ScreenComponent.getScaledX(30));
    reviseButton = new TextButton("Revise this Topic", skin, "body");
    addActor(reviseButton);
    reviseButton.setVisible(false);
    reviseButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        // Add tutors into learning stack and go there
        tutorHelper.pushRevisionMode();
      }
    });
  }
  
  private void showExplanation() {
    if (explanation == null || current >= explanation.length) return;
    
    String currentText = explanation[current];
    getNextButton().setVisible(current < explanation.length - 1);
    if (!getNextButton().isVisible()) {
      Actor goalNextButton = getStage().getRoot().findActor(ScreenComponent.NextButton.name());
      if (goalNextButton.isVisible()) {
        currentText += "\n\nTouch Next to continue";
      }
    }
    getPrevButton().setVisible(current > 0);
    boolean isImage = currentText.startsWith("image:");
    TextureRegion textureRegion = null;
    if (isImage) {
      textureRegion = ScienceEngine.getTextureRegion(currentText.substring("image:".length()));
    }
    if (textureRegion != null) {
      TextureRegionDrawable drawable = new TextureRegionDrawable(textureRegion);
      setImageAndResize(drawable);
      reviseButton.setPosition(getWidth() / 2 - reviseButton.getWidth() / 2, getHeight());
    } else {
      setTextAndResize(currentText, true);
      reviseButton.setPosition(getWidth() / 2 - reviseButton.getWidth() / 2, getHeight());
      if (true) ScienceEngine.getPlatformAdapter().speak(currentText, false);
    }
    
    reviseButton.setVisible(hasRevisionRefs);
  }

  /**
   * 
   * @param hasRevisionRefs 
   * @param explanation, not null
   */
  public void setExplanation(String[] explanation, boolean hasRevisionRefs) {
    current = 0;
    this.explanation = explanation;
    this.hasRevisionRefs = hasRevisionRefs;
    showExplanation();
  }
}
