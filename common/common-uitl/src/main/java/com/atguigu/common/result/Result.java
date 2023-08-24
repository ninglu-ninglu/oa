package com.atguigu.common.result;

import lombok.Data;

/*
@time 2023/8/14-18:21
@authon cheny
@name 哈哈
@version 1.0
统一返回数据
*/
@Data
public class Result<T> {
    //状态吗，返回信息，数据
    private Integer code;
    private String message;
    private T data;

    private Result(){
    }

    public static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        if (body!=null){
            result.setData(body);
        }
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }







    //返回成功的
    public static<T> Result<T> ok(){
        return build(null,ResultCodeEnum.SUCCESS);
    }
    public static<T> Result<T> ok(T date){
        return build(date,ResultCodeEnum.SUCCESS);
    }

    //返回失败的
    public static<T> Result<T> fail(){
        return build(null,ResultCodeEnum.FAIL);
    }
    public static<T> Result<T> fail(T date){
        return build(date,ResultCodeEnum.FAIL);
    }

    //自定义状态码，和信息
    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }

}
