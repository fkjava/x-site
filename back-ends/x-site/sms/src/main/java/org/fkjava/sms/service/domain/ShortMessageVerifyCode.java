package org.fkjava.sms.service.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("SMS")
public class ShortMessage {

    @Id
    private String id;
    
}
