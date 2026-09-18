package org.example.model;

import java.util.List;

public record EmailMessage(
    String subject,
    String date,
    String from,
    String to,
    List<EmailTurn> turns
) {}
