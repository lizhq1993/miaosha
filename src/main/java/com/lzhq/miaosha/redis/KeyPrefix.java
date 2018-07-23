package com.lzhq.miaosha.redis;

public interface KeyPrefix {
    int expireSeconds();
    String getPrefix();
}
