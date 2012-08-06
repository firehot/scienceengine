package com.mazalearn.scienceengine.screens;

import java.util.Locale;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.ValueChangedListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

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
    table.add("Options").colspan(3);

    // create the labels widgets
    final CheckBox soundEffectsCheckbox = new CheckBox("", getSkin());
    soundEffectsCheckbox.setChecked(game.getPreferencesManager()
        .isSoundEnabled());
    soundEffectsCheckbox.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        boolean enabled = soundEffectsCheckbox.isChecked();
        game.getPreferencesManager().setSoundEnabled(enabled);
        game.getSoundManager().setEnabled(enabled);
        game.getSoundManager().play(ScienceEngineSound.CLICK);
      }
    });
    table.row();
    table.add("Sound Effects");
    table.add(soundEffectsCheckbox).colspan(2).left();

    final CheckBox musicCheckbox = new CheckBox("", getSkin());
    musicCheckbox.setChecked(game.getPreferencesManager().isMusicEnabled());
    musicCheckbox.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        boolean enabled = musicCheckbox.isChecked();
        game.getPreferencesManager().setMusicEnabled(enabled);
        game.getMusicManager().setEnabled(enabled);
        game.getSoundManager().play(ScienceEngineSound.CLICK);

        // if the music is now enabled, start playing the menu music
        if (enabled)
          game.getMusicManager().play(ScienceEngineMusic.MENU);
      }
    });
    table.row();
    table.add("Music");
    table.add(musicCheckbox).colspan(2).left();

    // range is [0.0,1.0]; step is 0.1f
    Slider volumeSlider = new Slider(0f, 1f, 0.1f, getSkin());
    volumeSlider.setValue(game.getPreferencesManager().getVolume());
    volumeSlider.setValueChangedListener(new ValueChangedListener() {
      @Override
      public void changed(Slider slider, float value) {
        game.getPreferencesManager().setVolume(value);
        game.getMusicManager().setVolume(value);
        game.getSoundManager().setVolume(value);
        updateVolumeLabel();
      }
    });

    // create the volume label
    volumeValue = new Label("", getSkin());
    updateVolumeLabel();

    // add the volume row
    table.row();
    table.add("Volume");
    table.add(volumeSlider);
    table.add(volumeValue).width(40);

    // register the back button
    TextButton backButton = new TextButton("Back to main menu", getSkin());
    backButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        game.getSoundManager().play(ScienceEngineSound.CLICK);
        game.setScreen(new MenuScreen(game));
      }
    });
    table.row();
    table.add(backButton).size(250, 60).colspan(3);
  }

  /**
   * Updates the volume label next to the slider.
   */
  private void updateVolumeLabel() {
    float volume = (game.getPreferencesManager().getVolume() * 100);
    volumeValue.setText(String.format(Locale.US, "%1.0f%%", volume));
  }
}
