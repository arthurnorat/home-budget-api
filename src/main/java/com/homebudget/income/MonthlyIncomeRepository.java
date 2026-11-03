package com.homebudget.income;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

import static com.homebudget.FirestoreSupport.await;

@Repository
public class MonthlyIncomeRepository {

    private static final String COLLECTION = "monthly_income";

    private final Firestore firestore;

    public MonthlyIncomeRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    // The month (yyyy-MM) is the document id, so each month has at most one income
    public Optional<MonthlyIncome> findById(String month) {
        DocumentSnapshot snapshot = await(document(month).get(), "read income");
        if (!snapshot.exists()) {
            return Optional.empty();
        }
        return Optional.of(new MonthlyIncome(month, snapshot.getLong("amount").intValue()));
    }

    public MonthlyIncome save(MonthlyIncome income) {
        await(document(income.getMonth()).set(Map.of("amount", income.getAmount())), "save income");
        return income;
    }

    private DocumentReference document(String month) {
        return firestore.collection(COLLECTION).document(month);
    }
}
