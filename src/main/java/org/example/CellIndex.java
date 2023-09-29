package org.example;

class CellIndex {
    private int columnIndex;
    private int rowIndex;

    public CellIndex(int columnIndex, int rowIndex) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}
