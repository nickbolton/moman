package net.deuce.moman.report;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.deuce.moman.account.model.Account;
import net.deuce.moman.account.service.AccountService;
import net.deuce.moman.envelope.service.EnvelopeService;
import net.deuce.moman.model.EntityEvent;
import net.deuce.moman.model.EntityListener;
import net.deuce.moman.service.ServiceNeeder;
import net.deuce.moman.transaction.model.InternalTransaction;
import net.deuce.moman.transaction.service.TransactionService;
import net.deuce.moman.util.CalendarUtil;
import net.deuce.moman.util.DataDateRange;

import org.eclipse.birt.chart.device.ICallBackNotifier;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateRangeCombo;

public abstract class AbstractTransactionReportCanvas extends AbstractReportCanvas
implements EntityListener<InternalTransaction>, ICallBackNotifier {

	private AccountService accountService;
	private TransactionService transactionService;
	private EnvelopeService envelopeService;

	public AbstractTransactionReportCanvas(Composite parent, final DateRangeCombo combo, int style) {
		super(parent, combo, style);
		
		accountService = ServiceNeeder.instance().getAccountService();
		envelopeService = ServiceNeeder.instance().getEnvelopeService();
		transactionService = ServiceNeeder.instance().getTransactionService();
		transactionService.addEntityListener(this);
	}
	
	protected DataSetResult createDataSet(boolean expense) {
		double maxSum = 0.0;
		double minSum = Double.MAX_VALUE;
		List<Double> dataSet = new LinkedList<Double>();
		List<Account> accounts = accountService.getSelectedAccounts();
		
		Map<Account, List<InternalTransaction>> accountTransactions = new HashMap<Account, List<InternalTransaction>>();
		
		for (DataDateRange ddr : getDateRange().dataDateRanges()) {
			double sum = 0.0;
			for (Account account : accounts) {
				List<InternalTransaction> transactions = accountTransactions.get(account);
				if (transactions == null) {
					transactions = transactionService.getAccountTransactions(account, false);
					accountTransactions.put(account, transactions);
				}
				
				for (InternalTransaction it : transactions) {
					if (!it.isEnvelopeTransfer() &&
							((expense && it.getAmount() <= 0.0) || (!expense && it.getAmount() > 0.0))) {
						if (CalendarUtil.dateInRange(it.getDate(), ddr)) {
							sum += expense ? -it.getAmount() : it.getAmount();
						}
					}
				}
			}
			if (sum > maxSum) {
				maxSum = sum;
			}
			if (sum < minSum) {
				minSum = sum;
			}
			
			dataSet.add(sum);
		}
		
		return new DataSetResult(dataSet, minSum, maxSum);
	}
	
	public AccountService getAccountService() {
		return accountService;
	}
	
	public EnvelopeService getEnvelopeService() {
		return envelopeService;
	}
	
	public TransactionService getTransactionService() {
		return transactionService;
	}
	
	@Override
	public void entityAdded(EntityEvent<InternalTransaction> event) {
	}

	@Override
	public void entityChanged(EntityEvent<InternalTransaction> event) {
	}

	@Override
	public void entityRemoved(EntityEvent<InternalTransaction> event) {
	}
}
