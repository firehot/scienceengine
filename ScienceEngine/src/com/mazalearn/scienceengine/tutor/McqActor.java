package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.TopicHomeScreen;

public class McqActor extends Group {
  private static final int MAX_OPTIONS = 6;
  private TextButton[] optionButtons;
  private McqActor.OptionListener optionListener;
  
  private static class OptionListener extends ClickListener {
    
    private final TextButton[] optionButtons;
    private ITutor tutor;
    private boolean singleAnswer;

    public OptionListener(TextButton[] optionButtons) {
      this.optionButtons = optionButtons;
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
    }
    
    @Override
    public void clicked (InputEvent event, float x, float y) {
      Button thisButton = (Button) event.getListenerActor();
      if (singleAnswer && thisButton.isChecked()) {
        // Clear all buttons except this one.
        for (Button optionButton: optionButtons) {
          if (optionButton != thisButton) {
            optionButton.setChecked(false);
          }
        }
      }
      tutor.delegateeHasFinished(false);
    }
  }

  public McqActor(Skin skin) {
    super();
    this.optionListener = createListener(skin);
  }

  private McqActor.OptionListener createListener(Skin skin) {
    float y = ScreenComponent.McqOption.getY();
    this.optionButtons = new TextButton[MAX_OPTIONS];
    McqActor.OptionListener listener = new OptionListener(optionButtons); 
    for (int i = 0; i < MAX_OPTIONS; i++) {
      TextButton optionButton = TopicHomeScreen.createTextButton("", 
          ScreenComponent.McqOption.getX(ScreenComponent.getScaledX(400)), 
          y - ScreenComponent.getScaledY(30 * 2 * i), 
          400, 30,
          skin.get("toggle", TextButtonStyle.class));
      optionButton.setColor(Color.YELLOW);
      this.addActor(optionButton);
      optionButton.addListener(listener);
      optionButtons[i] = optionButton;
    }
    
    return listener;
  }
  
  public TextButton[] setUp(ITutor tutor, List<String> optionList, boolean singleAnswer) {
    optionListener.setUp(tutor, optionList, singleAnswer);
    for (Button optionButton: optionButtons) {
      optionButton.setChecked(false);
      optionButton.clearActions();
    }
    return optionButtons;
  }
}