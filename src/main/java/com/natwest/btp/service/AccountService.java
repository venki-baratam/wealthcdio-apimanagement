package com.natwest.btp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.natwest.btp.dto.AccountResponse;
import com.natwest.btp.dto.BalanceResponse;
import com.natwest.btp.dto.TransactionResponse;
import com.natwest.btp.dto.TransferResponse;
import com.natwest.btp.exceptions.BankingException;
import com.natwest.btp.model.Account;
import com.natwest.btp.model.Transaction;
import com.natwest.btp.model.TransactionType;
import com.natwest.btp.repository.InMemoryAccountRepository;

@Service
public class AccountService {
	
	private static final BigDecimal MAX_BALANCE = new BigDecimal("99999999999999999.99");
	
	private final InMemoryAccountRepository repository;
	private Clock clock;

    public AccountService(InMemoryAccountRepository repository, Clock clock) {
    	this.repository = repository;
    	this.clock = clock;
    }
    
    public synchronized AccountResponse createAccount(BigDecimal openingBalance) {
    	BigDecimal normalizedOpeningBalance = normalizeBalance(openingBalance, true);
    	UUID accountId = UUID.randomUUID();
    	Account account = new Account(accountId, normalizedOpeningBalance, clock.instant());
    	repository.save(account);
    	return new AccountResponse(accountId, normalizedOpeningBalance, account.getCreatedAt());
    }
    
    public synchronized BalanceResponse getBalance(UUID accountId) {
    	Account account = requireAccount(accountId);
    	return new BalanceResponse(accountId, account.getBalance());
    }
    
    public synchronized TransactionResponse deposit(UUID accountId, BigDecimal amount) {
    	Account account = requireAccount(accountId);
    	BigDecimal normalizedAmount = normalizePositiveAmount(amount);
    	BigDecimal updatedBalance = account.getBalance().add(normalizedAmount);
    	validateBalance(updatedBalance, "The resulting balance exceeds the supported maximum");
    	
    	Instant timestamp = clock.instant();
    	Transaction tranaction = new Transaction(
    			UUID.randomUUID(),
    			accountId,
    			TransactionType.DEPOSIT,
    			normalizedAmount,
    			updatedBalance,
    			null,
    			"Deposit",
    			timestamp
    	);
    	account.applyTransaction(tranaction);
    	account.setBalance(updatedBalance);
    	return new TransactionResponse(tranaction);
    }
    
    public synchronized TransactionResponse withdraw(UUID accountId, BigDecimal amount) {
    	Account account = requireAccount(accountId);
    	BigDecimal normalizedAmount = normalizePositiveAmount(amount);
    	BigDecimal updatedBalance = account.getBalance().subtract(normalizedAmount);
    	if(updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
    		throw BankingException.insufficientFunds(accountId);
    	}
    	validateBalance(updatedBalance, "The resulting balance exceeds the supported maximum");
    	
    	Instant timestamp = clock.instant();
    	Transaction tranaction = new Transaction(
    			UUID.randomUUID(),
    			accountId,
    			TransactionType.WITHDRAWAL,
    			normalizedAmount,
    			updatedBalance,
    			null,
    			"Withdrawal",
    			timestamp
    	);
    	account.applyTransaction(tranaction);
    	account.setBalance(updatedBalance);
    	return new TransactionResponse(tranaction);
    }

    public synchronized TransferResponse transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
    	if(sourceAccountId.equals(destinationAccountId)) {
    		throw BankingException.sameAccountTransfer();
    	}
    	
    	Account source = requireAccount(sourceAccountId);
    	Account destination = requireAccount(destinationAccountId);
    	BigDecimal normalizedAmount = normalizePositiveAmount(amount);
    	
    	if(source.getBalance().compareTo(normalizedAmount) < 0) {
    		throw BankingException.insufficientFunds(sourceAccountId);
    	}
    	
    	BigDecimal updatedSourceBalance = source.getBalance().subtract(normalizedAmount);
    	BigDecimal updatedDestinationBalance = destination.getBalance().add(normalizedAmount);
    	
    	validateBalance(updatedSourceBalance, "The resulting balance exceeds the supported maximum");
    	validateBalance(updatedDestinationBalance, "The destination account balance exceeds the supported maximum");
    	
    	Instant timestamp = clock.instant();
    	UUID transferId = UUID.randomUUID();
    	
    	Transaction debit = new Transaction(
    			transferId,
    			sourceAccountId,
    			TransactionType.TRANSFER_OUT,
    			normalizedAmount,
    			updatedSourceBalance,
    			destinationAccountId,
    			"Transfer to "+ destinationAccountId,
    			timestamp
    	);
    	
    	Transaction credit = new Transaction(
    			transferId,
    			destinationAccountId,
    			TransactionType.TRANSFER_IN,
    			normalizedAmount,
    			updatedDestinationBalance,
    			sourceAccountId,
    			"Transfer from "+ sourceAccountId,
    			timestamp
    	);
    	
    	source.applyTransaction(debit);
    	destination.applyTransaction(credit);
    	source.setBalance(updatedSourceBalance);
    	destination.setBalance(updatedDestinationBalance);
    	
    	return new TransferResponse(transferId, new TransactionResponse(debit), new TransactionResponse(credit), normalizedAmount, timestamp );
    }
    public synchronized List<TransactionResponse> getTransactions(UUID accountId){
    	Account account = requireAccount(accountId);
    	return account.gettransactions().stream()
    			.sorted(Comparator.comparing(Transaction::timestamp))
    			.map(TransactionResponse::new)
    			.toList();
    }
    
    private Account requireAccount(UUID accountId) {
    	return repository.findById(accountId)
    			.orElseThrow(() -> BankingException.accountNotFound(accountId));
    }
    
    private BigDecimal normalizePositiveAmount(BigDecimal amount) {
    	if(amount == null) {
    		throw BankingException.invalidAmount("Amount is required");
    	}
    	
    	BigDecimal normalized = amount.stripTrailingZeros();
    	
    	if(normalized.signum() <= 0) {
    		throw BankingException.invalidAmount("Amount must be greater than zero");
    	}
    	
    	if(normalized.scale() > 2) {
    		throw BankingException.invalidAmount("Amount must have at most two decimal places ");
    	}
    	
    	if(normalized.compareTo(MAX_BALANCE) > 0) {
    		throw BankingException.invalidAmount("Amount must not exceed 99999999999999999.99");
    	}
    	
    	return normalized.setScale(2, RoundingMode.UNNECESSARY);
    }
    
    private BigDecimal normalizeBalance(BigDecimal balance, boolean allowZero) {
    	if(balance == null) {
    		BigDecimal.ZERO.setScale(2);
    	}
    	
    	BigDecimal normalized = balance.stripTrailingZeros();
    	
    	if(normalized.scale() > 2) {
    		throw BankingException.invalidAmount("Balance must have at most two decimal places ");
    	}
    	
    	if(normalized.signum() < 0) {
    		throw BankingException.invalidAmount("Balance cannot be negative");
    	}
    	
    	if(!allowZero && normalized.signum() == 0) {
    		throw BankingException.invalidAmount("Balance must be greater than zero");
    	}
    	
    	if(normalized.compareTo(MAX_BALANCE) > 0) {
    		throw BankingException.invalidAmount("The resulting balance exceeds the supported maximum");
    	}
    	
    	return normalized.setScale(2, RoundingMode.UNNECESSARY);
    }
    
    private void validateBalance(BigDecimal balance, String message) {
    	
    	if(balance.compareTo(BigDecimal.ZERO) < 0) {
    		throw BankingException.insufficientFunds(null);
    	}
    	if(balance.compareTo(MAX_BALANCE) > 0) {
    		throw BankingException.balanceLimitExceeded(message);
    	}
    }
    
    
    
}