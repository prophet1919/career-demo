package com.wzu.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc

class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("正常分页：page=1&size=1 返回 code=0 且只有 1 条数据")
    void pageNormal() throws Exception {
        mockMvc.perform(get("/users").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("size 越界：size=200 返回 code=400")
    void pageSizeOutOfRange() throws Exception {
        mockMvc.perform(get("/users").param("page", "1").param("size", "200"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("用户不存在：/users/999 返回 code=404")
    void userNotFound() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }
}