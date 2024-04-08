package com.neo.sdk;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.neo.sdk.chatgpt.common.Constants;
import com.neo.sdk.chatgpt.domain.ChatChoice;
import com.neo.sdk.chatgpt.domain.ChatCompletionRequest;
import com.neo.sdk.chatgpt.domain.ChatCompletionResponse;
import com.neo.sdk.chatgpt.domain.Message;
import com.neo.sdk.chatgpt.session.Configuration;
import com.neo.sdk.chatgpt.session.OpenAiSession;
import com.neo.sdk.chatgpt.session.OpenAiSessionFactory;
import com.neo.sdk.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class ApiTest {

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        // 1. 配置文件；
        // 1.1 官网原始 apiHost https://api.openai.com/ - 官网的Key可直接使用
        // 1.2 三方公司 apiHost https://pro-share-aws-api.zcyai.com/ - 需要找我获得 Key
        Configuration configuration = new Configuration();
        configuration.setApiHost("http://gpt35.gleeze.com:8080/");
        configuration.setApiKey("sk-usywctCXPkAOxhirR5hXxYmNwjBKrycHiAD5P4Iq80ZpZQB8");

        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        OpenAiSession openAiSession = factory.openSession();

        System.out.println("我是 OpenAI ChatGPT，请输入你的问题：");

        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(new ArrayList<>())
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .maxTokens(1024)
                .build();

        // 3. 等待输入
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String text = scanner.nextLine();

            chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content(text).build());

            EventSource eventSource = openAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if ("[DONE]".equalsIgnoreCase(data)) {
                        onClosed(eventSource);
                    }

                    ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                    List<ChatChoice> choices = chatCompletionResponse.getChoices();
                    for (ChatChoice chatChoice : choices) {
                        Message delta = chatChoice.getDelta();
                        if (Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;

                        // 应答完成
                        String finishReason = chatChoice.getFinishReason();
                        if ("stop".equalsIgnoreCase(finishReason)) {
                            onClosed(eventSource);
                            return;
                        }

                        // 发送信息
                        System.out.println(delta.getContent());
                    }
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    System.out.println(response.code());
                }

                @Override
                public void onClosed(EventSource eventSource) {
                    System.out.println("请输入你的问题：");
                }
            });
        }

    }

}
