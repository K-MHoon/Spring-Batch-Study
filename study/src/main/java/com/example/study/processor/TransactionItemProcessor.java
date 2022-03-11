package com.example.study.processor;

import com.example.study.entity.AccountSummary;
import com.example.study.entity.Transaction;
import com.example.study.repository.AccountSummaryRepository;
import com.example.study.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

@RequiredArgsConstructor
public class TransactionItemProcessor implements ItemProcessor<AccountSummary, AccountSummary> {

    private final TransactionRepository transactionRepository;

    @Override
    public AccountSummary process(AccountSummary item) throws Exception {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(item.getAccountNumber());
        transactions.forEach(t -> item.setCurrentBalance(item.getCurrentBalance() + t.getAmount()));
        return item;
    }
}
