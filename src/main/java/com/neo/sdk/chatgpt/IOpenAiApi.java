package com.neo.sdk.chatgpt;
import com.neo.sdk.chatgpt.domain.ChatCompletionRequest;
import com.neo.sdk.chatgpt.domain.ChatCompletionResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IOpenAiApi {


    String v1_chat_completions = "v1/chat/completions";

    /**
     * 问答模型；默认 GPT-3.5
     *
     * @param chatCompletionRequest 请求信息
     * @return 应答结果
     */
    @POST(v1_chat_completions)
    Single<ChatCompletionResponse> completions(@Body ChatCompletionRequest chatCompletionRequest);
}
