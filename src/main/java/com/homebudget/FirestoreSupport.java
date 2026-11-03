package com.homebudget;

import com.google.api.core.ApiFuture;
import com.homebudget.exception.PersistenceException;

import java.util.concurrent.ExecutionException;

public final class FirestoreSupport {

    private FirestoreSupport() {}

    // Firestore calls return a future; wait for the result and turn SDK failures into a domain exception
    public static <T> T await(ApiFuture<T> future, String action) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new PersistenceException("Firestore failed to " + action, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenceException("Interrupted while trying to " + action, e);
        }
    }
}
