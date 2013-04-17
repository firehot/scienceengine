package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;

public class ExplanationBox extends ImageMessageBox {

  int current;
  String[] explanation;
  private TextButton reviseButton;
  private boolean hasRevisionRefs;
  
  public ExplanationBox(final TutorHelper tutorHelper, Skin skin, String textureName) {
    super(skin, textureName, null);
    getNextButton().addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        current++;
        showExplanation();
      }            
    });
    getPrevButton().addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        current--;
        showExplanation();
      }            
    });
    reviseButton = new TextButton("Revise This", skin);
    addActor(reviseButton);
    reviseButton.setVisible(false);
    reviseButton.addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        // Add tutors into learning stack and go there
        tutorHelper.pushRevisionMode();
      }
    });
  }
  
  private void showExplanation() {
    if (explanation == null || current >= explanation.length) return;
    
    String currentText = explanation[current];
    getNextButton().setVisible(current < explanation.length - 1);
    getPrevButton().setVisible(current > 0);
    TextureRegion textureRegion = ScienceEngine.getTextureRegion(currentText);
    if (textureRegion != null) {
      TextureRegionDrawable drawable = new TextureRegionDrawable(textureRegion);
      setImageAndResize(drawable);
      reviseButton.setPosition(getWidth() / 2 - reviseButton.getWidth() / 2, getHeight() * 1.1f);
    } else {
      setTextAndResize(currentText);
      reviseButton.setPosition(getWidth() / 2 - reviseButton.getWidth() / 2, getHeight() * 0.8f);
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