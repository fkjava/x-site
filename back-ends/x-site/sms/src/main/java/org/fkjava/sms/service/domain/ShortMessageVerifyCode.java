package org.fkjava.sms.service.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@RedisHash("SMS")
@Getter @Setter
public class ShortMessageVerifyCode implements Serializable {

    @Id
    private String id;
    private String sessionId;
    private String phone;
    private Set<String> codes = new HashSet<>();
}
