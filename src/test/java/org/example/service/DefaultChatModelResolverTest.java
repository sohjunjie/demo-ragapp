package org.example.service;

import org.example.model.JudgeOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultChatModelResolverTest {

    @Mock
    private ChatModel defaultChatModel;

    @Mock
    private ChatModel geminiChatModel;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ObjectProvider<ChatModel> chatModelProvider;

    @Mock
    private ObjectProvider<Map<String, ChatModel>> registeredChatModelsProvider;

    @Mock
    private ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Test
    void testResolveChatClientWithDefaultModel() {
        when(chatModelProvider.getIfAvailable()).thenReturn(defaultChatModel);
        when(registeredChatModelsProvider.getIfAvailable()).thenReturn(Map.of("gemini-2.5-flash", geminiChatModel));
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.clone()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        DefaultChatModelResolver resolver = new DefaultChatModelResolver(
                chatModelProvider,
                registeredChatModelsProvider,
                chatClientBuilderProvider
        );

        ChatClient resolved = resolver.resolveChatClient(JudgeOptions.defaults());
        assertNotNull(resolved);
        verify(chatClientBuilder).clone();
    }

    @Test
    void testResolveChatClientWithSpecificRegisteredModel() {
        when(chatModelProvider.getIfAvailable()).thenReturn(defaultChatModel);
        when(registeredChatModelsProvider.getIfAvailable()).thenReturn(Map.of("gemini-custom", geminiChatModel));
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);

        DefaultChatModelResolver resolver = new DefaultChatModelResolver(
                chatModelProvider,
                registeredChatModelsProvider,
                chatClientBuilderProvider
        );

        ChatClient resolved = resolver.resolveChatClient(new JudgeOptions("gemini-custom", 0.2));
        assertNotNull(resolved);
    }
}
