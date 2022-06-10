package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBalanceRequest;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFunds;

public interface AccountsRepository {

	void createAccount(Account account) throws DuplicateAccountIdException;

	void transferAmount(TransferBalanceRequest transferBalanceRequest) throws InsufficientFunds;

	Account getAccount(String accountId);

	void clearAccounts();
}
