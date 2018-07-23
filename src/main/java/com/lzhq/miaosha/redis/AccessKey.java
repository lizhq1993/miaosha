package com.lzhq.miaosha.redis;

public class AccessKey extends BasePrefix {
    private AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // public static AccessKey access = new AccessKey(5, "ak");
    public static AccessKey withExpire(int seconds) {
        return new AccessKey(seconds, "ak");
    }
}
