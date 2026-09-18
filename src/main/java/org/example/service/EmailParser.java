package org.example.service;

import org.example.model.EmailMessage;
import org.example.model.EmailTurn;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailParser {

    private static final Pattern SUBJECT_PATTERN = Pattern.compile("^Subject:\\s*(.*)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("^Date:\\s*(.*)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final Pattern FROM_PATTERN = Pattern.compile("^From:\\s*(.*)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final Pattern TO_PATTERN = Pattern.compile("^To:\\s*(.*)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    // Matches standard reply headers, e.g.:
    // "On Sun, 11 Jan 2026 11:24:00 +0000, Nils Lindqvist <nlindqvist@coreplatform.internal> wrote:"
    private static final Pattern REPLY_SEPARATOR_PATTERN = Pattern.compile(
            "^[>\\s]*On\\s+(.+?),\\s+(.+?)\\s+wrote:\\s*$",
            Pattern.MULTILINE
    );

    public EmailMessage parse(String emailContent) {
        if (emailContent == null || emailContent.isBlank()) {
            return new EmailMessage("", "", "", "", Collections.emptyList());
        }

        // 1. Extract standard email headers
        String subject = extractHeader(emailContent, SUBJECT_PATTERN);
        String date = extractHeader(emailContent, DATE_PATTERN);
        String from = extractHeader(emailContent, FROM_PATTERN);
        String to = extractHeader(emailContent, TO_PATTERN);

        // 2. Separate email header block from body content
        String body = isolateBody(emailContent);

        // 3. Extract and order conversation turns chronologically
        List<EmailTurn> turns = parseTurns(body, from, date);

        return new EmailMessage(subject, date, from, to, turns);
    }

    private List<EmailTurn> parseTurns(String body, String topSender, String topDate) {
        List<EmailTurn> turns = new ArrayList<>();
        Matcher matcher = REPLY_SEPARATOR_PATTERN.matcher(body);

        List<Integer> startIndices = new ArrayList<>();
        List<Integer> endIndices = new ArrayList<>();
        List<String> timestamps = new ArrayList<>();
        List<String> senders = new ArrayList<>();

        while (matcher.find()) {
            startIndices.add(matcher.start());
            endIndices.add(matcher.end());
            timestamps.add(matcher.group(1).trim());
            senders.add(matcher.group(2).trim());
        }

        // If no quotation lines exist, treat the body as a single turn
        if (startIndices.isEmpty()) {
            turns.add(new EmailTurn(1, topSender, topDate, cleanBody(body)));
            return turns;
        }

        // Top message in the thread (most recent reply)
        String latestText = body.substring(0, startIndices.get(0));
        turns.add(new EmailTurn(startIndices.size() + 1, topSender, topDate, cleanBody(latestText)));

        // Intermediate and initial quoted messages
        for (int i = 0; i < startIndices.size(); i++) {
            int turnContentStart = endIndices.get(i);
            int turnContentEnd = (i + 1 < startIndices.size()) ? startIndices.get(i + 1) : body.length();

            String rawTurnText = body.substring(turnContentStart, turnContentEnd);
            int turnNumber = startIndices.size() - i;

            turns.add(new EmailTurn(
                    turnNumber,
                    senders.get(i),
                    timestamps.get(i),
                    cleanBody(rawTurnText)
            ));
        }

        // Sort chronologically: Turn 1 (earliest) -> Turn N (latest)
        turns.sort(Comparator.comparingInt(EmailTurn::turnNumber));
        return turns;
    }

    private String isolateBody(String content) {
        int splitIndex = content.indexOf("\n\n");
        if (splitIndex == -1) {
            splitIndex = content.indexOf("\r\n\r\n");
        }
        return (splitIndex != -1) ? content.substring(splitIndex).trim() : content.trim();
    }

    private String cleanBody(String text) {
        if (text == null) {
            return "";
        }
        // Strip leading quotation markers ('>') and surrounding whitespace
        return text.replaceAll("(?m)^[>\\s]+", "").trim();
    }

    private String extractHeader(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }
}
