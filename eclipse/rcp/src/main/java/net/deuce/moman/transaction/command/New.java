package net.deuce.moman.transaction.command;

import java.util.Date;

import net.deuce.moman.account.ui.SelectAccountDialog;
import net.deuce.moman.entity.ServiceProvider;
import net.deuce.moman.entity.model.account.Account;
import net.deuce.moman.entity.model.transaction.InternalTransaction;
import net.deuce.moman.entity.model.transaction.TransactionFactory;
import net.deuce.moman.entity.model.transaction.TransactionStatus;
import net.deuce.moman.entity.service.account.AccountService;
import net.deuce.moman.entity.service.envelope.EnvelopeService;
import net.deuce.moman.entity.service.transaction.TransactionService;
import net.deuce.moman.transaction.ui.RegisterView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class New extends AbstractHandler {

	public static final String ID = "net.deuce.moman.transaction.command.new";

	private AccountService accountService = ServiceProvider.instance().getAccountService();

	private EnvelopeService envelopeService = ServiceProvider.instance().getEnvelopeService();

	private TransactionService transactionService = ServiceProvider.instance().getTransactionService();

	private TransactionFactory transactionFactory = ServiceProvider.instance().getTransactionFactory();

	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		Account account = null;
		if (accountService.getEntities().size() == 1) {
			account = accountService.getEntities().get(0);
		} else {
			SelectAccountDialog dialog = new SelectAccountDialog(window
					.getShell());
			dialog.create();
			if (dialog.open() == Window.OK) {
				account = dialog.getEntity();
			}
		}

		if (account != null) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()[0]
						.showView(RegisterView.ID, null,
								IWorkbenchPage.VIEW_ACTIVATE);
				InternalTransaction transaction = transactionFactory.newEntity(
						null, 0.0, null, new Date(), "Set Description", null,
						null, null, null, TransactionStatus.open, account);
				transaction.addSplit(envelopeService.getUnassignedEnvelope(),
						transaction.getAmount());
				envelopeService.clearSelectedEnvelope();
				transactionService.addEntity(transaction);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ExecutionException(e.getMessage(), e);
			}
		}

		return null;
	}
}
