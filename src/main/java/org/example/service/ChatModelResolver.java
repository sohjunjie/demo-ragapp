package org.example.service;

import org.example.model.JudgeOptions;
import org.springframework.ai.chat.client.ChatClient;

public interface ChatModelResolver {
    ChatClient resolveChatClient(JudgeOptions options);
}
