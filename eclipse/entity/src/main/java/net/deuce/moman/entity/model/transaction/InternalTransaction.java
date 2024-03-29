package net.deuce.moman.entity.model.transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.deuce.moman.entity.ServiceProvider;
import net.deuce.moman.entity.model.AbstractEntity;
import net.deuce.moman.entity.model.EntityProperty;
import net.deuce.moman.entity.model.account.Account;
import net.deuce.moman.entity.model.envelope.Envelope;
import net.deuce.moman.entity.service.transaction.TransactionService;
import net.sf.ofx4j.domain.data.common.TransactionType;

import org.dom4j.Document;

public class InternalTransaction extends AbstractEntity<InternalTransaction> {

	private static final long serialVersionUID = 1L;

    public enum Properties implements EntityProperty {
        imported(Boolean.class), externalId(String.class), amount(Double.class), type(TransactionType.class),
        date(Date.class), description(String.class), memo(String.class),
        check(String.class), ref(String.class), balance(Double.class), status(TransactionStatus.class),
        account(Account.class), split(Map.class), initialBalance(Double.class);
        
		private Class<?> ptype;
		
		public Class<?> type() { return ptype; }
		
		private Properties(Class<?> ptype) { this.ptype = ptype; }
    }
	
	private String externalId;
	private Double amount;
	private TransactionType type;
	private Date date;
	private String description;
	private String memo;
	private String check;
	private String ref;
	private Double balance;
	private Boolean initialBalance;
	private TransactionStatus status;

	private InternalTransaction transferTransaction;
	private Account account;
	private Map<Envelope, Split> splitMap = new HashMap<Envelope, Split>();
	
	private transient InternalTransaction matchedTransaction;
	private transient String transferTransactionId;
	private transient boolean imported;
	
	private transient TransactionService transactionService = ServiceProvider.instance().getTransactionService();
	
	// constants for annotations
	public static final int TYPE_LENGTH = 20;
	public static final int DESCRIPTION_LENGTH = 256;
	public static final int MEMO_LENGTH = 256;
	public static final int CHECK_NUMBER_LENGTH = 20;
	public static final int REF_NUMBER_LENGTH = 256;
	public static final int EXT_ID_LENGTH = 50;

	public InternalTransaction() {
		super();
	}
	
	
	public Document toXml() {
		return buildXml(Properties.values());
	}
	
	public boolean isImported() {
		return imported;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}

	public boolean isEnvelopeTransfer() {
		return transferTransaction != null;
	}
	
	public String getTransferTransactionId() {
		return transferTransactionId;
	}

	public void setTransferTransactionId(String transferTransactionId) {
		this.transferTransactionId = transferTransactionId;
	}

	public InternalTransaction getTransferTransaction() {
		return transferTransaction;
	}

	public void setTransferTransaction(InternalTransaction transferTransaction) {
		this.transferTransaction = transferTransaction;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		if (propertyChanged(this.status, status)) {
			this.status = status;
			getMonitor().fireEntityChanged(this, Properties.status);
		}
	}

	public Double getAmount() {
		return amount;
	}
	
	public void setAmount(Double amount, SplitSelectionHandler handler) {
		setAmount(amount, false, handler);
	}
	
	private boolean adjustSplits(double newAmount, SplitSelectionHandler handler) {
		if (splitMap.size() == 0) return true;
		
		List<Split> split = new LinkedList<Split>(splitMap.values());
		if (split.size() == 1) {
			split.get(0).setAmount(newAmount);
		} else if (handler != null) {
			return handler.handleSplitSelection(this, newAmount, split);
		}
		return true;
	}
	
	public void setAmount(Double amount, boolean adjustBalances, SplitSelectionHandler handler) {
		if (propertyChanged(this.amount, amount)) {
			double difference = 0.0;
			if (this.amount != null) {
				difference = this.amount - amount;
			}
			if (adjustSplits(amount, handler)) {
				this.amount = amount;
				if (amount > 0) {
					type = TransactionType.CREDIT;
				} else if (type == null || type != TransactionType.CHECK) {
					type = TransactionType.DEBIT;
				}
				if (balance != null) {
					setBalance(balance - difference);
					for (Split split : splitMap.values()) {
						split.getEnvelope().resetBalance();
					}
					getMonitor().fireEntityChanged(this, Properties.amount);
				}
				if (adjustBalances) {
					transactionService.adjustBalances(this, false);
				}
			}
		}
	}
	
	public boolean isInitialBalance() {
		return evaluateBoolean(initialBalance);
	}

	public Boolean getInitialBalance() {
		return initialBalance;
	}

	public void setInitialBalance(Boolean initialBalance) {
		this.initialBalance = initialBalance;
	}

	public boolean isMatched() {
		return matchedTransaction != null;
	}

	public void setMatchedTransaction(InternalTransaction transaction) {
		this.matchedTransaction = transaction;
	}
	
	public InternalTransaction getMatchedTransaction() {
		return matchedTransaction;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		if (propertyChanged(this.balance, balance)) {
			this.balance = balance;
			getMonitor().fireEntityChanged(this);
		}
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		if (propertyChanged(this.type, type)) {
			this.type = type;
			getMonitor().fireEntityChanged(this, Properties.type);
		}
	}

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		setDate(date, false);
	}
	
	public void setDate(Date date, boolean adjust) {
		if (propertyChanged(this.date, date)) {
			this.date = date;
			getMonitor().fireEntityChanged(this, Properties.date);
			
			if (adjust) {
				transactionService.adjustBalances(this, false);
			}
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (propertyChanged(this.description, description)) {
			this.description = description;
			getMonitor().fireEntityChanged(this);
		}
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		if (propertyChanged(this.memo, memo)) {
			this.memo = memo;
			getMonitor().fireEntityChanged(this);
		}
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		if (propertyChanged(this.check, check)) {
			this.check = check;
			getMonitor().fireEntityChanged(this);
		}
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		if (propertyChanged(this.ref, ref)) {
			this.ref = ref;
			getMonitor().fireEntityChanged(this);
		}
	}
	
	public boolean isExternal() {
		return externalId != null;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		if (propertyChanged(this.externalId, externalId)) {
			this.externalId = externalId;
			getMonitor().fireEntityChanged(this, Properties.externalId);
		}
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		if (propertyChanged(this.account, account)) {
			this.account = account;
			getMonitor().fireEntityChanged(this);
		}
	}
	
	public Double getSplitAmount(Envelope env) {
		Split split = splitMap.get(env);
		if (split != null) {
			return split.getAmount();
		} 
		return 0.0;
	}

	public void clearSplit() {
		for (Envelope env : splitMap.keySet()) {
			env.removeTransaction(this, false);
		}
		splitMap.clear();
		getMonitor().fireEntityChanged(this, Properties.split);
	}
	
	public void addSplit(Split item) {
		addSplit(item, true);
	}
	
	public void addSplit(Envelope envelope, Double amount) {
		addSplit(new Split(envelope, amount), true);
	}
	
	public void addSplit(Envelope envelope, Double amount, boolean notifyEnvelope) {
		addSplit(new Split(envelope, amount), notifyEnvelope);
	}
	
	public void addSplit(Split split, boolean notifyEnvelope) {
		if (!splitMap.containsValue(split)) {
			if (notifyEnvelope) {
				split.getEnvelope().addTransaction(this, false);
			}
			splitMap.put(split.getEnvelope(), split);
			getMonitor().fireEntityChanged(this, Properties.split);
		}
	}

	public void removeSplit(Envelope envelope) {
		removeSplit(envelope, true);
	}
	
	public void removeSplit(Envelope envelope, boolean notifyEnvelope) {
		if (notifyEnvelope) {
			envelope.removeTransaction(this, false);
		}
		
		if (splitMap.remove(envelope) != null) {
			envelope.resetBalance();
			getMonitor().fireEntityChanged(this, Properties.split);
		}
	}
	
	public List<Split> getSplit() {
		return new LinkedList<Split>(splitMap.values());
	}
	
	public void setSplit(List<Split> splitList) {
		setSplit(splitList, true);
	}
	
	public void setSplit(List<Split> splitList, boolean notify) {
		clearSplit();
		for (Split split : splitList) {
			addSplit(split, notify);
		}
	}
	
	
	public int compareTo(InternalTransaction o) {
		return compare(this, o);
	}
	
	
	public int compare(InternalTransaction o1, InternalTransaction o2) {
		int dateCompare = o1.date.compareTo(o2.getDate());
		
		if (dateCompare == 0) {
			if (o1.externalId != null && o2.getExternalId() != null) {
				return o1.externalId.compareTo(o2.getExternalId());
			}
			if (o1.externalId != null) {
				return -1;
			}
			if (o2.externalId != null) {
				return 1;
			}
			return o1.description.compareTo(o2.description);
		}
		return dateCompare;
	}

	
    public boolean equals(Object o) {
		if (this == o) return true;
	    if (o == null || getClass() != o.getClass()) return false;

	    InternalTransaction it = (InternalTransaction) o;

	    if (externalId != null ? !externalId.equals(it.externalId) : it.externalId != null) return false;
        return !(getId() != null ? !getId().equals(it.getId()) : it.getId() != null);

    }

	
	public int hashCode() {
	    int result = getId() != null ? getId().hashCode() : 0;
	    result = 31 * result + (externalId != null ? externalId.hashCode() : 0);
	    return result;
	}

	
	public String toString() {
		return "InternalTransaction : " + getId() + " - " + description + " - " + amount;
	}
	
	
	public String getRootName() {
		return "transaction";
	}
	
}
