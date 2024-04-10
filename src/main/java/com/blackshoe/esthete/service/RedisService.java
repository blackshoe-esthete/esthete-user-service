package com.blackshoe.esthete.service;

public interface RedisService {
    String getData(String key);
    void setData(String key,String value);
    boolean existsKey(String key);
    void setDataExpire(String key,String value,long duration);
    void deleteData(String key);

}
