package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;

public class Controller {
  final Cell<Table> cellTable;
  final IControl control;
  private static int CONFIG_HEIGHT = 40;

  public Controller(Cell<Table> cellTable, IControl control) {
    this.cellTable = cellTable;
    this.control = control;
  }

  public void validate() {
    control.syncWithModel();
    cellTable.getWidget().visible = control.isAvailable();
    cellTable.height(control.isAvailable() ? CONFIG_HEIGHT : 0);
  }
}