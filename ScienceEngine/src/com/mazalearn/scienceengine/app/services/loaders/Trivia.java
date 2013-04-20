package com.mazalearn.scienceengine.app.services.loaders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;

public class Trivia {
    
  private Array<?> trivia;
  public enum Part {
    text, image;
  }

  public Trivia() {
  }
   
  public void load() {
    Gdx.app.log(ScienceEngine.LOG, "Opening trivia file");
    FileHandle file = Gdx.files.internal("data/Electromagnetism/trivia.json");
    if (file == null) {
      Gdx.app.error(ScienceEngine.LOG, "Could not open trivia file");
      trivia = null;
    }
    String str = file.readString();
    trivia = (Array<?>) new JsonReader().parse(str);
  }

  public int getNumTrivia() {
    return trivia.size;
  }
  
  @SuppressWarnings("unchecked")
  public String getTriviumPart(int i, Part part) {
    OrderedMap<String, ?> trivium = (OrderedMap<String, ?>) trivia.get(i);
    return (String) trivium.get(part.name());
  }
}