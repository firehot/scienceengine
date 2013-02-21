package com.mazalearn.gwt.server;

public class Activity {
  static class LevelTutor {
    String type;
    String id;
    String goal;
    String group;
    LevelTutor[] childTutors;
  }
  String name;
  String description;
  int level;
  LevelTutor[] plan;
}
