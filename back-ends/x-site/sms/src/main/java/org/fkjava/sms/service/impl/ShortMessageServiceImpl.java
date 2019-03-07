package org.fkjava.sms.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import org.fkjava.sms.service.ShortMessageService;
import org.fkjava.sms.service.domain.ShortMessageVerifyCode;
import org.fkjava.sms.service.repository.ShortMessageVerifyCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ShortMessageServiceImpl implements ShortMessageService {

    private final ShortMessageVerifyCodeRepository repository;
    private final RedisTemplate<String, Object> template;

    private Random random = new Random();

    @Autowired
    public ShortMessageServiceImpl(
            ShortMessageVerifyCodeRepository repository,
            @Qualifier("serialRedisTemplate") RedisTemplate<String, Object> template) {
        this.repository = repository;
        this.template = template;
    }

    @Override
    public boolean sendVerifyCode(String sessionId, String phone) {
        String id = sessionId + "_" + phone;

        ShortMessageVerifyCode codes = this.template.execute(new SessionCallback<ShortMessageVerifyCode>() {
            @Override
            public <K, V> ShortMessageVerifyCode execute(RedisOperations<K, V> redisOperations) throws DataAccessException {
                return (ShortMessageVerifyCode) redisOperations.boundValueOps((K) id).get();
            }
        });
        codes.setId(id);
        codes.setId(sessionId + "_" + phone);
        codes.setSessionId(sessionId);
        codes.setPhone(phone);

        // 生成一个长度为4的数字验证码
        String verifyCode = generateNumberCode(4);

        codes.getCodes().add(verifyCode);

        // 保存对象到Redis数据库里面
        template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.boundValueOps(codes.getId()).set(codes);
                return null;
            }
        });

        // 设置15分钟过期时间
        template.expire(codes.getId(), 15, TimeUnit.MINUTES);

        // 把短信通过阿里云发送到用户手机
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAIab2ajidnpUNj", "B0XGcFiv87fOhLBdnsHi7DCXUPqG9q");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", codes.getPhone());
        request.putQueryParameter("SignName", "疯狂软件");
        request.putQueryParameter("TemplateCode", "SMS_90645001");
        request.putQueryParameter("TemplateParam", "{\"code\":\"" + verifyCode + "\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return true;
    }

    private String generateNumberCode(int length) {
        char[] cs = new char[length];
        for (int i = 0; i < cs.length; i++) {
            char c = (char) (random.nextInt(10) + '0');
            cs[i] = c;
        }
        return new String(cs);
    }

    @Override
    public boolean verify(String sessionId, String phone, String userCode) {

        String id = sessionId + "_" + phone;

        ShortMessageVerifyCode codes = (ShortMessageVerifyCode) template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                return redisOperations.boundValueOps(id).get();
            }
        });

        if (codes != null && codes.getCodes() != null) {
            for (String code : codes.getCodes()) {
                if (code.equalsIgnoreCase(userCode)) {
                    // 验证通过，删除所有关联的验证码
                    template.delete(id);
                    return true;
                }
            }
        }
        return false;
    }
}
