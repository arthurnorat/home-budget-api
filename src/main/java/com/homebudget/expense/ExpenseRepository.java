package com.homebudget.expense;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.WriteBatch;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.homebudget.FirestoreSupport.await;

@Repository
public class ExpenseRepository {

    private static final String COLLECTION = "expenses";

    private final Firestore firestore;

    public ExpenseRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Expense save(Expense expense) {
        prepareForSave(expense);
        await(document(expense.getExpenseId()).set(toDocument(expense)), "save expense");
        return expense;
    }

    // One batch = one atomic write (Firestore allows up to 500 writes per batch)
    public List<Expense> saveAll(List<Expense> expenses) {
        WriteBatch batch = firestore.batch();
        for (Expense expense : expenses) {
            prepareForSave(expense);
            batch.set(document(expense.getExpenseId()), toDocument(expense));
        }
        await(batch.commit(), "save expenses");
        return expenses;
    }

    public Optional<Expense> findById(UUID id) {
        DocumentSnapshot snapshot = await(document(id).get(), "read expense");
        return snapshot.exists() ? Optional.of(fromSnapshot(snapshot)) : Optional.empty();
    }

    public boolean existsById(UUID id) {
        return await(document(id).get(), "check expense").exists();
    }

    public void deleteById(UUID id) {
        await(document(id).delete(), "delete expense");
    }

    // date is stored as an ISO string, so text order is the same as date order
    public List<Expense> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end) {
        return await(collection()
                        .whereGreaterThanOrEqualTo("date", start.toString())
                        .whereLessThanOrEqualTo("date", end.toString())
                        .orderBy("date", Query.Direction.DESCENDING)
                        .get(), "list expenses")
                .getDocuments()
                .stream()
                .map(this::fromSnapshot)
                .toList();
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    private DocumentReference document(UUID id) {
        return collection().document(id.toString());
    }

    // Firestore does not generate UUIDs, so the id and creation time are set here
    private void prepareForSave(Expense expense) {
        if (expense.getExpenseId() == null) {
            expense.setExpenseId(UUID.randomUUID());
        }
        if (expense.getCreatedAt() == null) {
            // Firestore timestamps have microsecond precision
            expense.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        }
    }

    private Map<String, Object> toDocument(Expense expense) {
        Map<String, Object> data = new HashMap<>();
        data.put("description", expense.getDescription());
        data.put("amount", expense.getAmount());
        data.put("date", expense.getDate().toString());
        data.put("category", expense.getCategory().name());
        data.put("createdAt", Timestamp.ofTimeSecondsAndNanos(
                expense.getCreatedAt().getEpochSecond(), expense.getCreatedAt().getNano()));
        return data;
    }

    private Expense fromSnapshot(DocumentSnapshot snapshot) {
        Expense expense = new Expense();
        expense.setExpenseId(UUID.fromString(snapshot.getId()));
        expense.setDescription(snapshot.getString("description"));
        expense.setAmount(snapshot.getLong("amount").intValue());
        expense.setDate(LocalDate.parse(snapshot.getString("date")));
        expense.setCategory(Category.valueOf(snapshot.getString("category")));
        Timestamp createdAt = snapshot.getTimestamp("createdAt");
        expense.setCreatedAt(Instant.ofEpochSecond(createdAt.getSeconds(), createdAt.getNanos()));
        return expense;
    }
}
