package net.deuce.moman.account.ui;

import net.deuce.moman.account.command.Delete;
import net.deuce.moman.account.command.Edit;
import net.deuce.moman.entity.ServiceProvider;
import net.deuce.moman.entity.model.EntityEvent;
import net.deuce.moman.entity.model.account.Account;
import net.deuce.moman.entity.service.EntityService;
import net.deuce.moman.entity.service.account.AccountService;
import net.deuce.moman.ui.AbstractEntityTableView;
import net.deuce.moman.ui.SelectingTableViewer;

import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;

public class AccountView extends AbstractEntityTableView<Account> {

	public static final String ID = AccountView.class.getName();
	public static final String ACCOUNT_VIEWER_NAME = "account";

	private AccountService accountService = ServiceProvider.instance().getAccountService();

	public AccountView() {
		super();
	}

	protected EntityService<Account> getService() {
		return accountService;
	}

	protected String getViewerName() {
		return ACCOUNT_VIEWER_NAME;
	}

	protected boolean getLinesVisible() {
		return false;
	}

	protected SelectingTableViewer createTableViewer(Composite parent) {
		SelectingTableViewer tableViewer = new SelectingTableViewer(parent,
				SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);

		TableViewerColumn column = new TableViewerColumn(tableViewer,
				SWT.CENTER);
		column.getColumn().setText("enabled");
		column.getColumn().setWidth(30);
		column.setEditingSupport(new AccountEditingSupport(tableViewer, 0));

		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setText("name");
		column.getColumn().setWidth(200);
		// column.setEditingSupport(new AccountEditingSupport(tableViewer, 1));

		tableViewer.setContentProvider(new AccountContentProvider());
		tableViewer.setLabelProvider(new AccountLabelProvider());

		tableViewer.getTable().addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent evt) {
				IHandlerService handlerService = (IHandlerService) getSite()
						.getService(IHandlerService.class);
				try {
					handlerService.executeCommand(Edit.ID, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return tableViewer;
	}

	protected ColumnViewerEditorActivationStrategy createColumnViewerEditorActivationStrategy(
			TableViewer viewer) {
		return new ColumnViewerEditorActivationStrategy(viewer) {
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
	}

	protected boolean getHeaderVisible() {
		return false;
	}

	protected String getDeleteCommandId() {
		return Delete.ID;
	}

	public void createPartControl(Composite parent) {
		IContextService service = (IContextService) getSite().getService(
				IContextService.class);
		service.activateContext("net.deuce.moman.context.main");
		super.createPartControl(parent);
	}

	public void entityAdded(EntityEvent<Account> event) {
		super.entityAdded(event);
	}
}
