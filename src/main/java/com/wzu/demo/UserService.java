package com.wzu.demo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    public UserService(PasswordService passwordService) {
        //预置两个用户
        String p = passwordService.encode("123456");
        save(new User(null, "张三", p, "USER"));
        save(new User(null, "李四", p, "USER"));
    }

    public User save(User u){
        if(u.getId() == null){
            u.setId(idGen.incrementAndGet());
            u.setCreateTime(System.currentTimeMillis());
        }
        store.put(u.getId(), u);
        return u;
    }

    public User findByUsername(String username){
        return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }

    public User findById(Long id){
        return store.get(id);
    }

    public List<User> page(int page, int size){
        return store.values().stream().skip((page - 1) * size).limit(size).collect(Collectors.toList());
    }

}
