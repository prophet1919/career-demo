package com.wzu.demo;

public class Result <T>{
    private int code;
    private String message;
    private T data;

    public Result(int code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data){
        return new Result<>(0,"ok",data);
    }
    public int getCode() {return code;}
    public String getMessage() {return message;}
    public T getData() {return data;}
}
