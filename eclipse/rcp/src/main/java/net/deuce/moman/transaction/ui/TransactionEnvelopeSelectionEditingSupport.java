package net.deuce.moman.transaction.ui;

import net.deuce.moman.ui.ShiftKeyAware;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;

public class TransactionEnvelopeSelectionEditingSupport extends EditingSupport {

	private CellEditor editor;

	public TransactionEnvelopeSelectionEditingSupport(ColumnViewer viewer,
			ShiftKeyAware shiftKeyAwareControl, Composite parent) {
		super(viewer);
		editor = new TransactionEnvelopeSelectionCellEditor(
				shiftKeyAwareControl, parent);
	}

	protected boolean canEdit(Object element) {
		return true;
	}

	public CellEditor getCellEditor(Object element) {
		return editor;
	}

	protected Object getValue(Object element) {
		return element;
	}

	protected void setValue(Object element, Object value) {
	}

}
