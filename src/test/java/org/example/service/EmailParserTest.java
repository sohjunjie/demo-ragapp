package org.example.service;

import org.example.model.EmailMessage;
import org.example.model.EmailTurn;
import org.junit.jupiter.api.Test;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.*;

class EmailParserTest {

    private final EmailParser parser = new EmailParser();

    @Test
    void testParseEmailChain001() {
        TikaDocumentReader tikaReader = new TikaDocumentReader(new FileSystemResource("dataset/EMAIL_CHAIN_001_RESOLVED.eml"));
        String emailContent = tikaReader.read().getFirst().getText();
        EmailMessage message = parser.parse(emailContent);

        assertNotNull(message);
        assertEquals(4, message.turns().size());

        EmailTurn turn1 = message.turns().get(0);
        assertEquals(1, turn1.turnNumber());
        assertTrue(turn1.body().contains("Primary Issue:"));
        assertTrue(turn1.body().contains("pacs.008 messages for instant credit transfers rejected"));

        EmailTurn turn4 = message.turns().get(3);
        assertEquals(4, turn4.turnNumber());
        assertTrue(turn4.body().contains("Confirming from the desk that the transactions have posted correctly"));
    }

    @Test
    void testParseEmailChain006() {
        TikaDocumentReader tikaReader = new TikaDocumentReader(new FileSystemResource("dataset/EMAIL_CHAIN_006_ABANDONED.eml"));
        String emailContent = tikaReader.read().getFirst().getText();
        EmailMessage message = parser.parse(emailContent);

        assertNotNull(message);
        assertEquals(3, message.turns().size());

        EmailTurn turn1 = message.turns().get(0);
        assertEquals(1, turn1.turnNumber());
        assertTrue(turn1.body().contains("The End-of-Day (EOD) financial close batch failed at Step 48"));

        EmailTurn turn3 = message.turns().get(2);
        assertEquals(3, turn3.turnNumber());
        assertTrue(turn3.body().contains("Following up on ticket BNK-2026006"));
    }
}

