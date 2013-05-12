package cz.topolik.xssfinder.ui;

import cz.topolik.xssfinder.PossibleXSSLine;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Tomas Polesovsky
 */
public class PossibleXSSLineTableModel extends AbstractTableModel {
    List<PossibleXSSLine> lines;

    public PossibleXSSLineTableModel(List<PossibleXSSLine> lines) {
        this.lines = lines;
    }

    public PossibleXSSLine getLine(int index){
        return lines.get(index);
    }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        switch(column){
            case 0: return "idx";
            default: return "";
        }
    }

    public int getRowCount() {
        return lines.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex){
            case 0: return String.valueOf(rowIndex + 1);
            case 1: return lines.get(rowIndex).toString();
        }
        return "";
    }

}
