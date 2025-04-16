package ru;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.models.Account;
import ru.services.AccountManagerService;
import ru.services.TransactionWorkerService;
import ru.repository.AccountRepository;

import java.util.UUID;
import java.util.List;

@SpringBootApplication
public class TransactionApp {

    public static void main(String[] args) {
        SpringApplication.run(TransactionApp.class, args);
    }

    @Bean
    public CommandLineRunner run(AccountManagerService managerService,
                                 TransactionWorkerService workerService,
                                 AccountRepository accountRepository) {
        return args -> {
            /* При запуске приложение должно создать четыре (или более)
               экземпляров объекта Account со случайными значениями ID и
               значениями money равным 10000 */
            for (int i = 0; i < 4; i++) {
                Account account = new Account(UUID.randomUUID().toString(), 10000);
                accountRepository.save(account);
            }

            // В приложении запускается несколько (не менее двух) независимых потоков.
            for (int i = 0; i < 3; i++) {
                workerService.startProcessing();
            }

            // После 30 выполненных транзакций приложение должно завершиться.
            while (!managerService.hasReachedTransactionLimit()) {
                Thread.sleep(1000);
            }

            List<Account> accounts = accountRepository.findAll();
            for (Account account : accounts) {
                System.out.println("Остаток на счете " + account.getId() + ": " + account.getMoney() + " единиц");
            }
            System.out.println("Приложение было завершено после 30 транзакций");
            System.exit(0);
        };
    }
}