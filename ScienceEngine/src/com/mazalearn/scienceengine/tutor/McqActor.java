package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;

public class McqActor extends Group {
  private static final int MAX_OPTIONS = 6;
  private TextButton[] optionButtons;
  private McqActor.OptionListener optionListener;
  private TextButton submitButton;
  private ITutor tutor;
  private TextButton explanation;
  
  private static class OptionListener extends ClickListener {
    
    private final TextButton[] optionButtons;
    private ITutor tutor;
    private boolean singleAnswer;
    private TextButton submitButton;

    public OptionListener(TextButton[] optionButtons, TextButton submitButton) {
      this.optionButtons = optionButtons;
      this.submitButton = submitButton;
    }
    
    public void setUp(ITutor tutor, List<String> optionList, boolean singleAnswer) {
      this.tutor = tutor;
      this.singleAnswer = singleAnswer;
      
      for (int i = 0; i < optionButtons.length; i++) {
        TextButton optionButton = optionButtons[i];
        if (i < optionList.size()) {
          optionButton.setText(optionList.get(i));
          optionButton.setVisible(true);
        } else {
          optionButton.setVisible(false);
        }
      }
      submitButton.setVisible(true);
    }
    
    @Override
    public void clicked (InputEvent event, float x, float y) {
      if (tutor.getState() == ITutor.State.Finished) return;
      Button thisButton = (Button) event.getListenerActor();
      ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      if (singleAnswer && thisButton.isChecked()) {
        // Clear all buttons except this one.
        for (Button optionButton: optionButtons) {
          if (optionButton != thisButton) {
            optionButton.setChecked(false);
          }
        }
      }
      submitButton.setVisible(true);
    }
  }

  public McqActor(Skin skin) {
    super();
    createExplanationArea(skin);
    createSubmitButton(skin);
    this.optionListener = createListener(skin, submitButton);
  }

  private void createExplanationArea(Skin skin) {
    TextureRegion textureRegion = ScienceEngine.getTextureRegion("explanation");
    explanation = ScreenUtils.createImageButton(textureRegion, skin);
    explanation.getLabel().setWrap(true);
    TextButtonStyle tbs = new TextButtonStyle(skin.get(TextButtonStyle.class));
    tbs.fontColor = Color.BLACK;
    explanation.setStyle(tbs);
    explanation.setWidth(250);
    explanation.setHeight(250);
    explanation.setPosition(ScreenComponent.Explanation.getX(explanation.getWidth()),
        ScreenComponent.Explanation.getY(explanation.getHeight()));
    addActor(explanation);
  }

  private OptionListener createListener(Skin skin, TextButton submitButton) {
    float y = ScreenComponent.McqOption.getY();
    this.optionButtons = new TextButton[MAX_OPTIONS];
    McqActor.OptionListener listener = new OptionListener(optionButtons, submitButton); 
    for (int i = 0; i < MAX_OPTIONS; i++) {
      TextButton optionButton = ScreenUtils.createTextButton("", 
          ScreenComponent.McqOption.getX(ScreenComponent.getScaledX(400)), 
          y - ScreenComponent.getScaledY(30 * 2 * i), 
          400, 30,
          skin.get("mcq", TextButtonStyle.class));
      optionButton.getLabel().setAlignment(Align.center, Align.left);
      this.addActor(optionButton);
      optionButton.addListener(listener);
      optionButtons[i] = optionButton;
    }
    
    return listener;
  }
  
  public TextButton[] setUp(ITutor tutor, List<String> optionList, String reason, boolean singleAnswer) {
    this.tutor = tutor;
    optionListener.setUp(tutor, optionList, singleAnswer);
    for (Button optionButton: optionButtons) {
      optionButton.setChecked(false);
      optionButton.clearActions();
      Color c = optionButton.getColor();
      optionButton.setColor(c.r, c.g, c.b, 1);
    }
    submitButton.setVisible(false);
    explanation.setText(reason != null ? reason : "");
    explanation.setVisible(false);
    return optionButtons;
  }
  
  private void createSubmitButton(Skin skin) {
    TextButtonStyle style = new TextButtonStyle(skin.get("body", TextButtonStyle.class));
    style.font = skin.getFont(ScreenComponent.getFont(2));
    submitButton = new TextButton("Submit", style);
    submitButton.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        submitButton.setVisible(false);
        if (explanation.getText().length() > 0) {
          explanation.setVisible(true);
        }
        tutor.systemReadyToFinish(false);
      }
    });
    submitButton.setPosition(ScreenComponent.NextButton.getX(submitButton.getWidth()),
        ScreenComponent.NextButton.getY(submitButton.getHeight()));
    addActor(submitButton);
  }

}