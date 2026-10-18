package org.jeecg.modules.airag.app.controller;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.airag.app.service.IAiragChatService;
import org.jeecg.modules.airag.app.service.impl.AiragChatRateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AI聊天接口错误协议测试。
 *
 * @author scott
 * @since 2026-07-21 【issues/9787】无效分享链接错误提示
 */
@ExtendWith(MockitoExtension.class)
class AiragChatControllerTest {

    @Mock
    private IAiragChatService chatService;

    @Mock
    private AiragChatRateLimitService rateLimitService;

    @InjectMocks
    private AiragChatController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .build();
    }

    @Test
    void shouldReturnSseErrorWhenShareAccessValidationFails() throws Exception {
        when(chatService.send(any())).thenThrow(new JeecgBootException("分享链接无效或已取消发布"));

        MvcResult result = mockMvc.perform(post("/airag/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\",\"appId\":\"invalid-app\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"event\":\"ERROR\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("分享链接无效或已取消发布")));
    }
}
