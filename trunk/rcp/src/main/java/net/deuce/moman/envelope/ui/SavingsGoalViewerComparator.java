package net.deuce.moman.envelope.ui;

import net.deuce.moman.envelope.model.Envelope;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class SavingsGoalViewerComparator extends ViewerComparator {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return Envelope.SAVINGS_GOAL_COMPARATOR.compare((Envelope)e1, (Envelope)e2);
	}

}