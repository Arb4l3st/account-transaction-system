package ru.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.concurrent.locks.ReentrantLock;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "money")
    private int money;

    @Transient
    private final ReentrantLock lock = new ReentrantLock();

    public boolean hasSum(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public void deposit(int amount) {
        money += amount;
    }

}