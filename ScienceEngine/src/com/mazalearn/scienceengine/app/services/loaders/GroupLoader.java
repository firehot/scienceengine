package com.mazalearn.scienceengine.app.services.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class GroupLoader {

  static void loadGroups(Array<?> groups, IScience2DView science2DView) {
    science2DView.removeLocationGroups();
  
    if (groups == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading groups");
    
    for (int i = 0; i < groups.size; i++) {
      @SuppressWarnings("unchecked")
      Array<String> group = (Array<String>) groups.get(i);
      GroupLoader.loadGroup(group, science2DView);
    }
  }

  private static void loadGroup(Array<String> group, IScience2DView science2DView) {
    Gdx.app.log(ScienceEngine.LOG, "Loading group");
    Actor[] groupElements = new Actor[group.size];
    for (int i = 0; i < group.size; i++) {
      String name = group.get(i);
      Actor actor = science2DView.findActor(name);
      if (actor == null) {
        throw new IllegalArgumentException("Actor not found: " + name);
      }
      groupElements[i] = actor;      
    }
    science2DView.addLocationGroup(groupElements);
  }

}
