package net.deuce.moman.entity.service.fi.impl;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.deuce.moman.entity.model.account.Account;
import net.deuce.moman.entity.model.account.AccountFactory;
import net.deuce.moman.entity.model.fi.FinancialInstitution;
import net.deuce.moman.entity.service.fi.FinancialInstitutionService;
import net.deuce.moman.entity.service.impl.EntityServiceImpl;
import net.deuce.moman.entity.service.transaction.TransactionFetchResult;
import net.sf.ofx4j.client.AccountStatement;
import net.sf.ofx4j.client.BankAccount;
import net.sf.ofx4j.client.context.DefaultApplicationContext;
import net.sf.ofx4j.client.context.OFXApplicationContextHolder;
import net.sf.ofx4j.client.impl.BaseFinancialInstitutionData;
import net.sf.ofx4j.domain.data.banking.AccountType;
import net.sf.ofx4j.domain.data.banking.BankAccountDetails;
import net.sf.ofx4j.domain.data.banking.BankAccountInfo;
import net.sf.ofx4j.domain.data.signup.AccountProfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("financialInstitutionService")
public class FinancialInstitutionServiceImpl extends EntityServiceImpl<FinancialInstitution>
implements FinancialInstitutionService {

	private Map<String, FinancialInstitution> financialInstitutionsByName = new HashMap<String, FinancialInstitution>();
	
	@Autowired
	private AccountFactory accountFactory;
	
	public boolean doesFinancialInstitutionExistByName(String name) {
		return financialInstitutionsByName.containsKey(name);
	}
	
	public FinancialInstitution getFinancialInstitutionByName(String name) {
		FinancialInstitution fi = financialInstitutionsByName.get(name);
		if (fi == null) {
			throw new RuntimeException("No financial institution exists with name: " + name);
		}
		return fi;
	}
	
	public void addEntity(FinancialInstitution financialInstitution) {
		if (financialInstitutionsByName.containsKey(financialInstitution.getName())) {
			throw new RuntimeException("Duplicate financial institution name: " + financialInstitution.getName());
		}
		financialInstitutionsByName.put(financialInstitution.getName(), financialInstitution);
		super.addEntity(financialInstitution);
	}
	
	public void removeFinancialInstitution(FinancialInstitution financialInstitution) {
		financialInstitutionsByName.remove(financialInstitution.getName());
		super.removeEntity(financialInstitution);
	}
	
	public TransactionFetchResult fetchTransactions(Account account, Date startDate, Date endDate) throws Exception {
		
		OFXApplicationContextHolder.setCurrentContext(new DefaultApplicationContext("QWIN", "1700"));

		BaseFinancialInstitutionData data = new BaseFinancialInstitutionData(account.getFinancialInstitution().getFinancialInstitutionId());
		data.setOFXURL(new URL(account.getFinancialInstitution().getUrl()));
		data.setOrganization(account.getFinancialInstitution().getOrganization());
		data.setFinancialInstitutionId(account.getFinancialInstitution().getFinancialInstitutionId());
		net.sf.ofx4j.client.FinancialInstitutionService service = new net.sf.ofx4j.client.impl.FinancialInstitutionServiceImpl();
		net.sf.ofx4j.client.FinancialInstitution fi = service.getFinancialInstitution(data);

		// read the fi profile (note: not all institutions
		// support this, and you normally don't need it.)
		// FinancialInstitutionProfile profile = fi.readProfile();

		// get a reference to a specific bank account at your FI
		BankAccountDetails bankAccountDetails = new BankAccountDetails();

		// routing number to the bank.
		bankAccountDetails.setRoutingNumber(account.getBankId());
		// bank account number.
		bankAccountDetails.setAccountNumber(account.getAccountId());
		// it's a checking account
		bankAccountDetails.setAccountType(AccountType.CHECKING);

		BankAccount bankAccount = fi.loadBankAccount(bankAccountDetails, account.getUsername(), account.getPassword());

		AccountStatement statement = null;
		
		statement = bankAccount.readStatement(startDate, endDate);
		
		/*
		FileWriter fileWriter = new FileWriter("/Users/nbolton/Downloads/importedTransactions.ofx");
		try {
			StatementRange range = new StatementRange();
		    range.setIncludeTransactions(true);
		    range.setStart(startDate);
		    range.setEnd(endDate);

		    FinancialInstitutionWrapper wiggy = new FinancialInstitutionWrapper(data, new OFXV1Connection());
		    RequestEnvelope request = wiggy.createRequest(account.getUsername(), account.getPassword());
		    TransactionWrappedRequestMessage requestTransaction = new BankStatementRequestTransaction();
		    BankStatementRequest bankRequest = new BankStatementRequest();
		    bankRequest.setAccount(bankAccountDetails);
		    bankRequest.setStatementRange(range);
		    requestTransaction.setWrappedMessage(bankRequest);
		    BankingRequestMessageSet bankingRequest = new BankingRequestMessageSet();
		    bankingRequest.setStatementRequest((BankStatementRequestTransaction) requestTransaction);
		    request.getMessageSets().add(bankingRequest);

		    ResponseEnvelope response = new OFXV1Connection().sendRequest(request, data.getOFXURL());
			OFXV2Writer writer = new OFXV2Writer(fileWriter);
			AggregateMarshaller marshaller = new AggregateMarshaller();
			marshaller.marshal(response, writer);
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
		*/
		
		return new TransactionFetchResult(
				statement.getLedgerBalance().getAmount(),
				endDate,
				statement.getTransactionList().getTransactions());
	}
	
	public List<Account> getAvailableAccounts(FinancialInstitution financialInstitution,
			String username, String password) throws Exception {
		List<Account> list = new LinkedList<Account>();
		
		OFXApplicationContextHolder.setCurrentContext(new DefaultApplicationContext("QWIN", "1700"));

		BaseFinancialInstitutionData data = new BaseFinancialInstitutionData(financialInstitution.getFinancialInstitutionId());
		data.setOFXURL(new URL(financialInstitution.getUrl()));
		data.setOrganization(financialInstitution.getOrganization());
		data.setFinancialInstitutionId(financialInstitution.getFinancialInstitutionId());
		net.sf.ofx4j.client.FinancialInstitutionService service = new net.sf.ofx4j.client.impl.FinancialInstitutionServiceImpl();
		net.sf.ofx4j.client.FinancialInstitution fi = service.getFinancialInstitution(data);

		Collection<AccountProfile> profiles = fi.readAccountProfiles(username, password);
		Account account;
		
		for (AccountProfile profile : profiles) {
			BankAccountInfo bankInfo = profile.getBankSpecifics();
			account = accountFactory.newEntity(
				Boolean.TRUE,
				profile.getDescription(),
				bankInfo.getBankAccount().getBankId(),
				bankInfo.getBankAccount().getAccountNumber(),
				username,
				password,
				bankInfo.getStatus(),
				bankInfo.getSupportsTransactionDetailOperations(),
				0.0,
				0.0,
				0.0
				);
			list.add(account);
		}
		return list;
	}
	
	
	public boolean isClearable() {
		return false;
	}

	/*
	private static class FinancialInstitutionWrapper extends FinancialInstitutionImpl {

		public FinancialInstitutionWrapper(FinancialInstitutionData data, OFXConnection connection) {
			super(data, connection);
		}
		
		public RequestEnvelope createRequest(String username, String password) {
			return super.createAuthenticatedRequest(username, password);
		}
	}
	*/
}
