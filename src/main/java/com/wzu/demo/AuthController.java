package com.wzu.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度 3-20")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度 6-32")
        private String password;

        public String getUsername() {return username;}
        public void setUsername(String username) {this.username = username;}
        public String getPassword() {return password;}
        public void setPassword(String password) {this.password = password;}
    }

    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid  RegisterRequest req){
        if(userService.findByUsername(req.getUsername())!=null){
            throw new BizException(409, "用户名已存在");
        }
        User u = new User(null, req.getUsername(), req.getPassword(), "USER");
        return Result.ok(userService.save(u).getId());
    }
}
