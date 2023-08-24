package com.atguigu.common.config.handler;

import com.atguigu.common.result.ResultCodeEnum;

/*
@time 2023/8/15-9:06
@authon cheny
@name 哈哈
@version 1.0
*/
public class GuiguException extends RuntimeException{
    private  Integer code;
    private  String msg;

    public GuiguException(Integer code,String msg){
        super(msg);
        this.code=code;
        this.msg=msg;
    }

    public GuiguException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code=resultCodeEnum.getCode();
        this.msg=resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "GuiguException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
