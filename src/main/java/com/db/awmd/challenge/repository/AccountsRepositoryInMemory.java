package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBalanceRequest;
import com.db.awmd.challenge.exception.AccountDoesNotExistsException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.db.awmd.challenge.exception.InsufficientFunds;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.web.AccountsController;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private static final Logger log = LoggerFactory.getLogger(AccountsRepositoryInMemory.class);

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public void transferAmount(TransferBalanceRequest transferBalanceRequest) throws InsufficientFunds {
		String accountFrom = transferBalanceRequest.getFromAccountNumber();
		String accountTo = transferBalanceRequest.getToAccountNumber();
		BigDecimal amount = transferBalanceRequest.getAmount();
		Account accFrom = accounts.get(accountFrom);
		Account accTo = accounts.get(accountTo);
		if (accFrom == null) {
			throw new AccountDoesNotExistsException("Account From id " + accountFrom + " does not exists!");
		}
		if (accTo == null) {
			throw new AccountDoesNotExistsException("Account To id " + accountFrom + " does not exists!");
		}
		if (accFrom.getBalance().compareTo(amount) == 0 || accFrom.getBalance().compareTo(amount) == 1) {
			accFrom.setBalance(accFrom.getBalance().subtract(amount));
			accTo.setBalance(accTo.getBalance().add(amount));
			accounts.putIfAbsent(accFrom.getAccountId(), accFrom);
			accounts.putIfAbsent(accTo.getAccountId(), accTo);
			log.info("Funds successfully transfered.");
			EmailNotificationService emailNotificationService = new EmailNotificationService();
			emailNotificationService.notifyAboutTransfer(accFrom, "Funds successfully transfered.");

		} else {
			log.info("Insufficient funds");
			throw new InsufficientFunds("Account From id " + accountFrom + " is having insufficient funds!");
		}
	}

}
