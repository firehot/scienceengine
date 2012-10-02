package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.esotericsoftware.tablelayout.Cell;

public class Controller {
  final Cell<Table> cellTable;
  final IControl control;
  final Table table;

  public Controller(Cell<Table> cellTable, IControl control) {
    this.cellTable = cellTable;
    this.control = control;
    this.table = cellTable.getWidget();
  }

  public void validate() {
    control.syncWithModel();
    cellTable.setWidget(control.isAvailable() ? this.table : null);
  }
}