package com.ustp.tracker.model;

import java.time.LocalDate;

/** An academic task. A record is immutable, so its data can never be changed by accident. */
public record Task(int id, String name, String subject, String description, LocalDate deadline, String status) {
    public static final String PENDING = "Pending";
    public static final String COMPLETED = "Completed";

    public boolean isPending() { return PENDING.equals(status); }
}
