package com.wzu.demo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/users")
    public List<Map<String,Object>> users(){
        return List.of(
                Map.of("id", 1, "name","张三","age",20),
                Map.of("id", 2, "name","李四","age",21)
        );
    }
    @GetMapping("/users/{id}")
    public Map<String, Object> userById(@PathVariable int id){
        return Map.of("id", id, "name", "用户" + id, "age",20);
    }


}
