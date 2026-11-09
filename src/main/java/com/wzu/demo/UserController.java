package com.wzu.demo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/users")
    public Result<List<User>> users(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size){
        return Result.ok(userService.page(page, size));
    }

    @GetMapping("/users/{id}")
    public Result<User> userById(@PathVariable Long id){
        User u = userService.findById(id);
        if(u == null){
            throw new BizException(404, "用户不存在");
        }
        return Result.ok(u);
    }
}
