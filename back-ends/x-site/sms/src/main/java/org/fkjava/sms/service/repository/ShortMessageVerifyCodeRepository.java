package org.fkjava.sms.service.repository;

import org.fkjava.sms.service.domain.ShortMessageVerifyCode;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortMessageVerifyCodeRepository extends KeyValueRepository<ShortMessageVerifyCode, String> {
}
