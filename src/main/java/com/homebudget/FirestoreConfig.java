package com.homebudget;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirestoreConfig {

    // Project id comes from GOOGLE_CLOUD_PROJECT; credentials come from the runtime identity (Cloud Run).
    // With FIRESTORE_EMULATOR_HOST set, the client talks to the local emulator without credentials.
    @Bean
    public Firestore firestore() {
        return FirestoreOptions.getDefaultInstance().getService();
    }
}
