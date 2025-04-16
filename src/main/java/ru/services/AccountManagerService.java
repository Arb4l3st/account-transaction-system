package ru.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.models.Account;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AccountManagerService {

    private final AtomicInteger transactionCounter = new AtomicInteger(0);
    private static final int TRANSACTION_LIMIT = 30;

    /**
     * Выполнение перевода между счетами
     * @param from - счет списания
     * @param to - счет для перевода
     * @param amount - сумма перевода
     * @return - возвращает true в случае успешного перевода, иначе false
     */
    public boolean transfer(Account from, Account to, int amount) {
        if (from == null || to == null || from.equals(to)) {
            log.warn("Попытка перевода не удалась: счет списания={}, счет для перевода={}", from, to);
            return false;
        }

        Account firstAcc = from.getId().compareTo(to.getId()) < 0 ? from : to;
        Account secondAcc = from == firstAcc ? to : from;
        firstAcc.getLock().lock();
        secondAcc.getLock().lock();

        try {
            if (from.hasSum(amount)) {
                to.deposit(amount);
                log.info("Выполнен перевод: Сумма {} со счета {} на счет {}", amount, from.getId(), to.getId());
                return true;
            } else {
                log.warn("Ошибка перевода: Недостаточно средств на счете {}", from.getId());
            }
        } catch (Exception e) {
            log.error("Ошибка выполнения перевода ", e);
        } finally {
            secondAcc.getLock().unlock();
            firstAcc.getLock().unlock();
        }

        return false;
    }

    public boolean hasReachedTransactionLimit() {
        return transactionCounter.get() >= TRANSACTION_LIMIT;
    }

    public void incrementTransaction() {
        transactionCounter.incrementAndGet();
    }
}