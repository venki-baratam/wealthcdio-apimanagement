package com.natwest.btp.exceptions;

import java.util.UUID;

public class BankingException extends RuntimeException {
	
	public enum Code{
		INVALID_AMOUNT,
		ACCOUNT_NOT_FOUND,
		INSUFFICIENT_FUNDS,
		SAME_ACCOUNT_TRANSFER,
		BALANCE_LIMIT_EXCEEDED
	}

	private final Code code;
	
    public BankingException(Code code, String message) {
        super(message);
        this.code = code;
    }
    
    public Code getCode() {
    	return code;
    }
    
    public static BankingException invalidAmount(String message) {
    	return new BankingException(Code.INVALID_AMOUNT, message);
    }
    
    public static BankingException accountNotFound(UUID accountId) {
    	return new BankingException(Code.ACCOUNT_NOT_FOUND,"Account not found: "+accountId);
    }
    
    public static BankingException insufficientFunds(UUID accountId) {
    	String message = accountId == null 
    			? "The account does not have enough funds for this operation."
    			:"Account " + accountId + " does not have enough funds for this operation.";
    	return new BankingException(Code.INSUFFICIENT_FUNDS, message);
    }
    
    public static BankingException sameAccountTransfer() {
    	return new BankingException(Code.SAME_ACCOUNT_TRANSFER,"Source and destination accounts must be different.");
    }
    
    public static BankingException balanceLimitExceeded(String message) {
    	return new BankingException(Code.BALANCE_LIMIT_EXCEEDED, message);
    }
}