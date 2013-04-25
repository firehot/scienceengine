package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;

public class McqActor extends Group {
  private static final int MAX_OPTIONS = 8;
  private McqActor.OptionListener optionListener;
  private TextButton submitButton;
  private ITutor tutor;
  private ButtonGroup optionButtons;
  private final CheckBoxStyle radioButtonStyle, checkBoxStyle;
  private Image questionImage;
  private Label progressInfo;
  
  private static class OptionListener extends ClickListener {
    
    private ITutor tutor;
    private TextButton submitButton;

    public OptionListener(TextButton submitButton) {
      this.submitButton = submitButton;
    }
    
    public void setTutor(ITutor tutor) {
      this.tutor = tutor;     
    }
    
    @Override
    public void clicked (InputEvent event, float x, float y) {
      if (tutor.getState() == ITutor.State.Finished) return;
      ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      submitButton.setVisible(true);
    }
  }

  public McqActor(Skin skin) {
    super();
    createSubmitButton(skin);
    this.optionListener = createListener(skin, submitButton);
    this.radioButtonStyle = skin.get("mcq-radio", CheckBoxStyle.class);
    this.checkBoxStyle = skin.get("mcq-check", CheckBoxStyle.class);
    this.progressInfo = new Label("", skin);
    this.addActor(progressInfo);
    
    progressInfo.setPosition(ScreenComponent.McqProgressInfo.getX(ScreenComponent.getScaledX(40)),
       ScreenComponent.McqProgressInfo.getY(ScreenComponent.getScaledY(30)));
    

    final TextureRegion blackTexture = ScreenUtils.createTextureRegion(10, 10, Color.BLACK);
    this.questionImage = new Image() {
      @Override
      public void draw(SpriteBatch batch, float parentAlpha) {
        // Black background required so that image shows in front of activity bodies
        batch.draw(blackTexture, getX(), getY(), getWidth() + 10, getHeight() + 10);
        super.draw(batch, parentAlpha);
      }
    };
    this.addActor(questionImage);
  }

  private OptionListener createListener(Skin skin, TextButton submitButton) {
    float y = ScreenComponent.McqOption.getY();
    optionButtons = new ButtonGroup();
    McqActor.OptionListener listener = new OptionListener(submitButton); 
    for (int i = 0; i < MAX_OPTIONS; i++) {
      TextButton optionButton = ScreenUtils.createImageCheckBox("", 
          ScreenComponent.McqOption.getX(ScreenComponent.getScaledX(400)), 
          y - ScreenComponent.getScaledY(30 * 2 * i), 
          400, 30,
          skin.get("mcq", CheckBoxStyle.class));
      // Store index in name for future use
      optionButton.setName(String.valueOf(i));
      optionButton.getLabel().setAlignment(Align.center, Align.left);
      this.addActor(optionButton);
      optionButton.addListener(listener);
      optionButtons.add(optionButton);
    }
    optionButtons.setMinCheckCount(0);
    
    return listener;
  }
  
  public ButtonGroup setUp(ITutor tutor, String progress, String questionImageTexture, List<String> optionList, boolean singleAnswer) {
    this.tutor = tutor;
    optionListener.setTutor(tutor);
    progressInfo.setText(progress);
    if (questionImageTexture != null) {
      TextureRegion textureRegion = ScienceEngine.getTextureRegion(questionImageTexture);
      if (textureRegion != null) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(textureRegion);
        questionImage.setDrawable(drawable);
        questionImage.setSize(drawable.getMinWidth(), drawable.getMinHeight());
      }
    } else {
      questionImage.setDrawable(null);
      questionImage.setSize(0, 0);
    }
    Array<Button> buttons = optionButtons.getButtons();
    setOptions(optionList, singleAnswer, buttons);
    placeOptionsOnScreen(buttons, optionList.size());
    optionButtons.setMaxCheckCount(singleAnswer ? 1 : -1);
    submitButton.setVisible(false);
    return optionButtons;
  }
  
  private void placeOptionsOnScreen(Array<Button> buttons, int numOptions) {
    float x = ScreenComponent.McqOption.getX(questionImage.getWidth());
    float y = ScreenComponent.McqOption.getY() - questionImage.getHeight() + ScreenComponent.getScaledY(30 * 2);
    questionImage.setPosition(x, y);
    for (int i = 0; i < numOptions; i++) {
      TextButton optionButton = (TextButton) buttons.get(i);
      optionButton.setY(y - ScreenComponent.getScaledY(30 * 2 * (i + 1)));
    }
  }

  private void setOptions(List<String> optionList, boolean singleAnswer,
      Array<Button> buttons) {
    for (int i = 0; i < buttons.size; i++) {
      TextButton optionButton = (TextButton) buttons.get(i);
      optionButton.setChecked(false);
      optionButton.clearActions();
      optionButton.setStyle(singleAnswer ? radioButtonStyle : checkBoxStyle);
      Color c = optionButton.getColor();
      optionButton.setColor(c.r, c.g, c.b, 1);
      if (i < optionList.size()) {
        String text = optionList.get(i);
        // TODO: for image MCQ 
        /*
          TextureRegion textureRegion = ScienceEngine.getTextureRegion(text.toLowerCase());
          TextureRegionDrawable image = new TextureRegionDrawable(textureRegion);
          optionButton.setBackground(image);
          optionButton.setSize(image.getMinWidth(), image.getMinHeight());
          */
        optionButton.setText(text);
        optionButton.setVisible(true);
      } else {
        optionButton.setVisible(false);
      }
    }
  }
  
  private void createSubmitButton(Skin skin) {
    TextButtonStyle style = new TextButtonStyle(skin.get("body", TextButtonStyle.class));
    style.font = skin.getFont(ScreenComponent.getFont(2));
    submitButton = new TextButton("Submit", style);
    submitButton.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        submitButton.setVisible(false);
        tutor.systemReadyToFinish(false);
      }
    });
    submitButton.setPosition(ScreenComponent.NextButton.getX(submitButton.getWidth()),
        ScreenComponent.NextButton.getY(submitButton.getHeight()));
    addActor(submitButton);
  }

}