package org.example.service;

import org.example.model.JudgeOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultChatModelResolver implements ChatModelResolver {

    private final ChatModel defaultChatModel;
    private final Map<String, ChatModel> registeredChatModels;
    private final ChatClient.Builder chatClientBuilder;

    @Autowired
    public DefaultChatModelResolver(
            ObjectProvider<ChatModel> chatModelProvider,
            ObjectProvider<Map<String, ChatModel>> registeredChatModelsProvider,
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider
    ) {
        this.defaultChatModel = chatModelProvider.getIfAvailable();
        this.registeredChatModels = registeredChatModelsProvider.getIfAvailable();
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null && this.defaultChatModel != null) {
            builder = ChatClient.builder(this.defaultChatModel);
        }
        this.chatClientBuilder = builder;
    }

    @Override
    public ChatClient resolveChatClient(JudgeOptions options) {
        ChatModel targetModel = defaultChatModel;
        if (options != null && options.modelName() != null && registeredChatModels != null) {
            ChatModel found = registeredChatModels.get(options.modelName());
            if (found != null) {
                targetModel = found;
            }
        }

        if (targetModel != null && (chatClientBuilder == null || targetModel != defaultChatModel)) {
            return ChatClient.builder(targetModel).build();
        }

        if (chatClientBuilder != null) {
            return chatClientBuilder.clone().build();
        }

        throw new IllegalStateException("No ChatModel or ChatClient.Builder configured. Please configure an LLM provider (e.g. Google GenAI / Gemini).");
    }
}
