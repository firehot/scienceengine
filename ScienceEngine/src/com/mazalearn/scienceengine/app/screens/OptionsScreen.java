package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

/**
 * A simple options screen.
 */
public class OptionsScreen extends AbstractScreen {
  private Label volumeValue;

  public OptionsScreen(ScienceEngine game) {
    super(game);
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  @Override
  public void show() {
    super.show();

    // retrieve the default table actor
    Table table = super.getTable();
    table.defaults().spaceBottom(30);
    table.columnDefaults(0).padRight(20);
    table.add(getMsg().getString("ScienceEngine.Options")).colspan(3); //$NON-NLS-1$

    // Create locale selection box if platform supports languages
    IPlatformAdapter platform = ScienceEngine.getPlatformAdapter();
    if (platform.supportsLanguage()) {
      final SelectBox languageSelect = 
          new SelectBox(new String[] { "en", "ka", "hi"}, getSkin());
      languageSelect.setSelection(getMsg().getLanguage());
      languageSelect.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          getMsg().setLanguage(getSkin(), languageSelect.getSelection());
        }
      });
      table.row();
      table.add(getMsg().getString("ScienceEngine.Language")); // $NON-NLS-1$
      table.add(languageSelect).colspan(2).left();
    }
    
    // create the labels widgets
    final CheckBox soundEffectsCheckbox = new CheckBox("", getSkin()); //$NON-NLS-1$
    soundEffectsCheckbox.setChecked(ScienceEngine.getPreferencesManager()
        .isSoundEnabled());
    soundEffectsCheckbox.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        boolean enabled = soundEffectsCheckbox.isChecked();
        ScienceEngine.getPreferencesManager().setSoundEnabled(enabled);
        ScienceEngine.getSoundManager().setEnabled(enabled);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      }
    });
    table.row();
    table.add(getMsg().getString("ScienceEngine.SoundEffects")); //$NON-NLS-1$
    table.add(soundEffectsCheckbox).colspan(2).left();

    if (platform.supportsMusic()) {
      final CheckBox musicCheckbox = new CheckBox("", getSkin()); //$NON-NLS-1$
      musicCheckbox.setChecked(ScienceEngine.getPreferencesManager().isMusicEnabled());
      musicCheckbox.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          boolean enabled = musicCheckbox.isChecked();
          ScienceEngine.getPreferencesManager().setMusicEnabled(enabled);
          ScienceEngine.getMusicManager().setEnabled(enabled);
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
  
          // if the music is now enabled, start playing the menu music
          if (enabled)
            ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);
        }
      });
      table.row();
      table.add(getMsg().getString("ScienceEngine.Music")); //$NON-NLS-1$
      table.add(musicCheckbox).colspan(2).left();
    }

    // range is [0.0,1.0]; step is 0.1f
    final Slider volumeSlider = new Slider(0f, 1f, 0.1f, false, getSkin());
    volumeSlider.setValue(ScienceEngine.getPreferencesManager().getVolume());
    volumeSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float value = volumeSlider.getValue();
        ScienceEngine.getPreferencesManager().setVolume(value);
        ScienceEngine.getMusicManager().setVolume(value);
        ScienceEngine.getSoundManager().setVolume(value);
        updateVolumeLabel();
      }
    });

    // create the volume label
    volumeValue = new Label("", getSkin()); //$NON-NLS-1$
    updateVolumeLabel();

    // add the volume row
    table.row();
    table.add(getMsg().getString("ScienceEngine.Volume")); //$NON-NLS-1$
    table.add(volumeSlider);
    table.add(volumeValue).width(40);
    
    // Add About
    table.row();
    TextButton aboutButton = 
        new TextButton(getMsg().getString("ScienceEngine.About"), getSkin());
    aboutButton.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        new AboutDialog(getSkin()).show(stage);
      }      
    });
    table.add(aboutButton).colspan(3);
  }

  /**
   * Updates the volume label next to the slider.
   */
  private void updateVolumeLabel() {
    float volume = (ScienceEngine.getPreferencesManager().getVolume() * 100);
    volumeValue.setText(String.valueOf(volume));
  }
  
  @Override
  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    scienceEngine.setScreen(new SplashScreen(scienceEngine));
  }
  
}
