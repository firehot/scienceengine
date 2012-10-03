package com.mazalearn.scienceengine.app.screens;

import java.util.Locale;

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
import com.mazalearn.scienceengine.app.services.Messages;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;

/**
 * A simple options screen.
 */
public class OptionsScreen extends AbstractScreen {
  private Label volumeValue;

  public OptionsScreen(ScienceEngine game) {
    super(game);
  }

  @Override
  public void show() {
    super.show();

    // retrieve the default table actor
    Table table = super.getTable();
    table.defaults().spaceBottom(30);
    table.columnDefaults(0).padRight(20);
    table.add(Messages.getString("ScienceEngine.Options")).colspan(3); //$NON-NLS-1$

    // Create locale selection box
    final SelectBox localeSelect = 
        new SelectBox(new String[] { "en", "ka", "hi"}, getSkin());
    localeSelect.setSelection(Messages.getLocale().getLanguage());
    localeSelect.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        Locale locale = new Locale(localeSelect.getSelection());
        Messages.setLocale(locale);
      }
    });
    table.row();
    table.add(Messages.getString("ScienceEngine.Language")); // $NON-NLS-1$
    table.add(localeSelect).colspan(2).left();
    
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
    table.add(Messages.getString("ScienceEngine.SoundEffects")); //$NON-NLS-1$
    table.add(soundEffectsCheckbox).colspan(2).left();

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
    table.add(Messages.getString("ScienceEngine.Music")); //$NON-NLS-1$
    table.add(musicCheckbox).colspan(2).left();

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
    volumeValue = new Label("", ScienceEngine.getSkin()); //$NON-NLS-1$
    updateVolumeLabel();

    // add the volume row
    table.row();
    table.add(Messages.getString("ScienceEngine.Volume")); //$NON-NLS-1$
    table.add(volumeSlider);
    table.add(volumeValue).width(40);

    // register the back button
    TextButton backButton = new TextButton(Messages.getString("ScienceEngine.BackToMainMenu"), getSkin()); //$NON-NLS-1$
    backButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(new StartScreen(scienceEngine));
      }
    });
    table.row();
    table.add(backButton).size(250, 60).colspan(3);
  }

  /**
   * Updates the volume label next to the slider.
   */
  private void updateVolumeLabel() {
    float volume = (ScienceEngine.getPreferencesManager().getVolume() * 100);
    volumeValue.setText(String.valueOf(volume));
  }
}
