package ru.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.models.Account;
import ru.repository.AccountRepository;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionWorkerService {

    private final AccountManagerService managerService;
    private final AccountRepository accountRepository;

    private final Random rand = new Random();

    @Async
    public void startProcessing() {
        List<Account> accounts = accountRepository.findAll();

        while (!managerService.hasReachedTransactionLimit()) {
            try {
                int sleepTime = rand.nextInt(1000) + 1000;
                log.info("Поток [{}] уснет на {} мс", Thread.currentThread().getName(), sleepTime);
                Thread.sleep(sleepTime);
                log.info("Поток [{}] начал работу", Thread.currentThread().getName());

                Account from = accounts.get(rand.nextInt(accounts.size()));
                Account to;

                do {
                    to = accounts.get(rand.nextInt(accounts.size()));
                } while (from.equals(to));

                double currentBalance = from.getMoney();

                // Сумма списания или зачисления определяется случайным образом.
                int amount = ThreadLocalRandom.current().nextInt(1, (int) currentBalance + 1);

                boolean success = managerService.transfer(from, to, amount);
                if (success) {
                    managerService.incrementTransaction();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Поток транзакций был прерван", e);
            } catch (Exception e) {
                log.error("Произошла непредвиденная ошибка при выполнении транзакции", e);
            }
        }

        log.info("Достигнут лимит транзакций");
    }
}