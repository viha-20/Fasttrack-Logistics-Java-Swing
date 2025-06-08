package views.components;

import javax.swing.table.DefaultTableModel;

public class CustomTableModel extends DefaultTableModel {
    private boolean[] editableColumns;

    public CustomTableModel(Object[] columnNames, int rowCount, boolean[] editableColumns) {
        super(columnNames, rowCount);
        this.editableColumns = editableColumns;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return editableColumns[column];
    }
}