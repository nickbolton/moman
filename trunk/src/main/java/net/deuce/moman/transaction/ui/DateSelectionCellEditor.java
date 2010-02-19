package net.deuce.moman.transaction.ui;

import java.util.Date;

import net.deuce.moman.Constants;
import net.deuce.moman.ui.DateSelectionDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class DateSelectionCellEditor extends ControlLimitingCellEditor {
	
	private Label label;
	private Date date;
	
	public DateSelectionCellEditor(Composite parent) {
		super(parent);
	}
	
	@Override
	protected Control createContents(Composite cell) {
		label = new Label(cell, SWT.NONE);
		return label;
	}

	@Override
	protected void updateContents(Object value) {
		date = (Date)value;
		if (date != null) {
			label.setText(Constants.SHORT_DATE_FORMAT.format(date));
		}
	}

	@Override
	public void activate() {
		openDialogBox(label);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		DateSelectionDialog dialog = new DateSelectionDialog(cellEditorWindow.getShell(), date);
		dialog.open();
		
		if (dialog.getDate() != null) {
			doSetValue(dialog.getDate());
			fireApplyEditorValue();
		}
		return null;
	}

}
