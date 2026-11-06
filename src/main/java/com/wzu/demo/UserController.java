package com.wzu.demo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@Validated
public class UserController {
    private final List<Map<String,Object>> userList = new ArrayList<>(List.of(
            Map.of("id",1,"name","张三","age",20),
            Map.of("id",2,"name","李四","age",21)
    ));
    @GetMapping("/users")
    public Result<List<Map<String,Object>>> users(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size){

        int from = (page - 1) * size;
        if(from > userList.size()){
            return Result.ok(List.of());
        }
        int to = Math.min(from + size, userList.size());
        return Result.ok(userList.subList(from, to));
    }
    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> userById(@PathVariable int id){
        for(Map<String,Object> u:userList){
            if(u.get("id").equals(id)){
                return Result.ok(u);
            }
        }
        throw new BizException(404, "用户不存在");
    }
}
