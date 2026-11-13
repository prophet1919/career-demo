package com.wzu.demo;

import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    private String auth;

    @BeforeEach
    void setUp() {
        // 用张三生成一个真 token，后面所有请求都带上
        User u = userService.findByUsername("张三");
        auth = "Bearer " + tokenService.create(u);
    }

    @Test
    @DisplayName("正常分页：page=1&size=1 返回 code=0 且只有 1 条数据")
    void pageNormal() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", auth).param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("size 越界：size=200 返回 code=400")
    void pageSizeOutOfRange() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", auth).param("page", "1").param("size", "200"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("用户不存在：/users/999 返回 code=404")
    void userNotFound() throws Exception {
        mockMvc.perform(get("/users/999").header("Authorization", auth))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("不带 token 访问 /users 返回 code=401")
    void noToken() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("带错 token 访问 /users 返回 code=401")
    void badToken() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(jsonPath("$.code").value(401));
    }
}