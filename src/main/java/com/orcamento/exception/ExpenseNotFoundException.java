package com.orcamento.exception;

import java.util.UUID;

public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(UUID id) {
        super("Gasto não encontrado: " + id);
    }
}
