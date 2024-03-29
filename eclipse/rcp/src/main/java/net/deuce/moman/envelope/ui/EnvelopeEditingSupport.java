package net.deuce.moman.envelope.ui;

import net.deuce.moman.RcpConstants;
import net.deuce.moman.entity.ServiceProvider;
import net.deuce.moman.entity.model.Frequency;
import net.deuce.moman.entity.model.envelope.Envelope;
import net.deuce.moman.entity.service.envelope.EnvelopeService;
import net.deuce.moman.ui.CurrencyCellEditorValidator;
import net.deuce.moman.undo.EntityUndoAdapter;
import net.deuce.moman.undo.UndoAdapter;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;

public class EnvelopeEditingSupport extends EditingSupport {

	private CellEditor editor;
	private int column;

	private EnvelopeService envelopeService = ServiceProvider.instance().getEnvelopeService();

	public EnvelopeEditingSupport(ColumnViewer viewer, int column) {
		super(viewer);

		String[] values;

		switch (column) {
		case 0:
			editor = new TextCellEditor(((TreeViewer) viewer).getTree());
			editor.getControl().setFont(RcpConstants.STANDARD_FONT);
			break;
		case 2:
			editor = new TextCellEditor(((TreeViewer) viewer).getTree());
			editor.getControl().setFont(RcpConstants.STANDARD_FONT);
			editor.setValidator(CurrencyCellEditorValidator.instance());
			break;
		case 3:
			values = new String[Frequency.values().length];
			for (int i = 0; i < Frequency.values().length; i++) {
				values[i] = Frequency.values()[i].label();
			}
			editor = new ComboBoxCellEditor(((TreeViewer) viewer).getTree(),
					values, SWT.READ_ONLY);
			editor.getControl().setFont(RcpConstants.STANDARD_FONT);
			break;
		default:
			editor = null;
		}
		this.column = column;
	}

	protected boolean canEdit(Object element) {
		Envelope envelope = (Envelope) element;

		if (envelope == envelopeService.getRootEnvelope())
			return false;

		if (column == 2 && envelope.hasChildren())
			return false;

		return envelope.isEditable();
	}

	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	protected Object getValue(Object element) {
		Envelope env = (Envelope) element;

		switch (this.column) {
		case 0:
			return env.getName();
		case 2:
			return RcpConstants.CURRENCY_VALIDATOR.format(env.getBudget());
		case 3:
			return env.getFrequency().ordinal();
		default:
			break;
		}
		return null;
	}

	protected void setValue(Object element, Object value) {

		if (value != null) {

			Envelope env = (Envelope) element;

			UndoAdapter undoAdapter = new EntityUndoAdapter<Envelope>(env);

			switch (this.column) {
			case 0:
				undoAdapter.executeChange(Envelope.Properties.name, value);
				break;
			case 2:
				undoAdapter.executeChange(Envelope.Properties.budget,
						RcpConstants.CURRENCY_VALIDATOR
								.validate((String) value).doubleValue());
				break;
			case 3:
				undoAdapter.executeChange(Envelope.Properties.frequency,
						Frequency.values()[(Integer) value]);
				break;
			default:
				break;
			}
			getViewer().update(element, null);
		}
	}

}
