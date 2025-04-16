package ru.repository;

import org.springframework.stereotype.Repository;
import ru.models.Account;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AccountRepository {

    private List<Account> accounts = new ArrayList<>();

    public void save(Account account) {
        accounts.add(account);
    }

    public List<Account> findAll() {
        return new ArrayList<>(accounts);
    }

}