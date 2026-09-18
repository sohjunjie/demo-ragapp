package org.example.model;

public record EmailTurn(
    int turnNumber,
    String sender,
    String timestamp,
    String body
) {}
