package com.mazalearn.scienceengine.experiments.config;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;

public class Config {
  final Cell<Table> cell;
  Condition condition = null;

  public Config(Cell<Table> cell) {
    this.cell = cell;
  }

  public void addCondition(Condition condition) {
    this.condition = condition;
  }
  
  void validate() {
    boolean visible = condition == null || condition.eval();
    cell.getWidget().visible = visible;
    cell.ignore(!visible);
  }
}