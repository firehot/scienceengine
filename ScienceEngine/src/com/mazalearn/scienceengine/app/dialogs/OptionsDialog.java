package com.mazalearn.scienceengine.app.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

/**
 * A simple options screen.
 */
public class OptionsDialog extends Dialog {
  private Label volumeValue;
  
  private static String getMsg(String msgId) {
    return ScienceEngine.getMsg().getString(msgId);
  }

  public OptionsDialog(final Stage stage, final Skin skin) {
    super(getMsg("ScienceEngine.Options"), skin);
    final PreferencesManager preferencesManager = ScienceEngine.getPreferencesManager();

    // retrieve the default table actor
    Table table = getContentTable();
    table.defaults().spaceBottom(ScreenComponent.getScaledY(10));
    table.columnDefaults(0).padRight(ScreenComponent.getScaledX(20));

    // Create locale selection box if platform supports languages
    final IPlatformAdapter platform = ScienceEngine.getPlatformAdapter();
    if (platform.supportsLanguage()) {
      final SelectBox languageSelect = 
          new SelectBox(new String[] { "en", "ka", "hi"}, skin);
      languageSelect.setSelection(ScienceEngine.getMsg().getLanguage());
      languageSelect.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          ScienceEngine.getMsg().setLanguage(skin, languageSelect.getSelection());
        }
      });
      table.row();
      table.add(getMsg("ScienceEngine.Language")); // $NON-NLS-1$
      table.add(languageSelect).colspan(2).left();
    }
    
    if (platform.supportsSync()) {
      final SelectBox syncSelect = 
          new SelectBox(new String[] { "Manual", "Automatic"}, skin);
      syncSelect.setSelection("Automatic");
      syncSelect.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          preferencesManager.setSync(syncSelect.getSelection());
        }
      });
      table.row();
      table.add(getMsg("ScienceEngine.SyncMode")); // $NON-NLS-1$
      table.add(syncSelect).left();
      TextButton syncButton = new TextButton(getMsg("ScienceEngine.ForceSync"), skin, "body");
      syncButton.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          preferencesManager.syncProfiles(true);
        }
      });
      table.add(syncButton).left();
    }
    // create the labels widgets
    final CheckBox soundEffectsCheckbox = new CheckBox("", skin); //$NON-NLS-1$
    soundEffectsCheckbox.setChecked(ScienceEngine.getPreferencesManager()
        .isSoundEnabled());
    soundEffectsCheckbox.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        boolean enabled = soundEffectsCheckbox.isChecked();
        preferencesManager.setSoundEnabled(enabled);
        ScienceEngine.getSoundManager().setEnabled(enabled);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      }
    });
    table.row();
    table.add(getMsg("ScienceEngine.SoundEffects")); //$NON-NLS-1$
    table.add(soundEffectsCheckbox).colspan(2).left();

    final CheckBox musicCheckbox = new CheckBox("", skin); //$NON-NLS-1$
    musicCheckbox.setChecked(ScienceEngine.getPreferencesManager().isMusicEnabled());
    musicCheckbox.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        boolean enabled = musicCheckbox.isChecked();
        preferencesManager.setMusicEnabled(enabled);
        ScienceEngine.getMusicManager().setEnabled(enabled);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);

        // if the music is now enabled, start playing the menu music
        if (enabled)
          ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);
      }
    });
    table.row();
    table.add(getMsg("ScienceEngine.Music")); //$NON-NLS-1$
    table.add(musicCheckbox).colspan(2).left();

    if (platform.supportsSpeech()) {
      final CheckBox speechCheckbox = new CheckBox("", skin); //$NON-NLS-1$
      speechCheckbox.setChecked(ScienceEngine.getPreferencesManager().isSpeechEnabled());
      speechCheckbox.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          boolean enabled = speechCheckbox.isChecked();
          preferencesManager.setSpeechEnabled(enabled);
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
  
          // if the music is now enabled, start playing the menu music
          if (enabled)
            platform.speak("Speech enabled", false);
        }
      });
      table.row();
      table.add(getMsg("ScienceEngine.Speech")); //$NON-NLS-1$
      table.add(speechCheckbox).colspan(2).left();
    }
    // range is [0.0,1.0]; step is 0.1f
    final Slider volumeSlider = new Slider(0f, 1f, 0.1f, false, skin);
    volumeSlider.setValue(ScienceEngine.getPreferencesManager().getVolume());
    volumeSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float value = volumeSlider.getValue();
        preferencesManager.setVolume(value);
        ScienceEngine.getMusicManager().setVolume(value);
        ScienceEngine.getSoundManager().setVolume(value);
        updateVolumeLabel();
      }
    });

    // create the volume label
    volumeValue = new Label("", skin); //$NON-NLS-1$
    updateVolumeLabel();

    // add the volume row
    table.row();
    table.add(getMsg("ScienceEngine.Volume")); //$NON-NLS-1$
    table.add(volumeSlider);
    table.add(volumeValue).width(40);
    
    // Add About
    table.row();
    TextButton aboutButton = 
        new TextButton(getMsg("ScienceEngine.About"), skin, "body");
    aboutButton.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        new AboutDialog(skin).show(stage);
      }      
    });
    table.add(aboutButton).colspan(3);
    
    getButtonTable().add(new TextButton("OK", skin)).width(150);
  }

  /**
   * Updates the volume label next to the slider.
   */
  private void updateVolumeLabel() {
    int volume = Math.round(ScienceEngine.getPreferencesManager().getVolume() * 100);
    volumeValue.setText(String.valueOf(volume));
  }
}
