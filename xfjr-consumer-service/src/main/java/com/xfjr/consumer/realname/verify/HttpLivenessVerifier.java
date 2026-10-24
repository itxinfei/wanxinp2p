package com.xfjr.consumer.realname.verify;

import com.alibaba.fastjson2.JSON;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 活体/人脸核验对接骨架（FR-135）：调用第三方活体检测/人脸比对服务。
 * 请求发送 OCR 姓名/身份证号 + 图像在 file-service 的引用 + 挑战类型；
 * 响应解析活体分数、人脸比对分数、攻击检测。未配置 url 或调用失败抛出异常（异常流①）。
 */
@Slf4j
@Service
public class HttpLivenessVerifier implements LivenessVerifier {

    private static final MediaType JSON_MT = MediaType.parse("application/json; charset=utf-8");

    @Value("${realname.liveness.url:}")
    private String url;
    @Value("${realname.liveness.timeout:5000}")
    private int timeoutMillis;
    @Value("${realname.liveness.app-key:}")
    private String appKey;

    @Override
    public LivenessVerifyResult verify(LivenessVerifyRequest request) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("活体检测服务未配置(realname.liveness.url)");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("appKey", appKey);
        body.put("consumerNo", request.getConsumerNo());
        body.put("name", request.getRealName());
        body.put("idCard", request.getIdCard());
        body.put("livenessImageRef", request.getLivenessImageRef());
        body.put("faceImageRef", request.getFaceImageRef());
        body.put("challengeType", request.getChallengeType());

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .build();
        String json = JSON.toJSONString(body);
        Request httpRequest = new Request.Builder().url(url)
                .post(RequestBody.create(json, JSON_MT)).build();
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("活体检测服务返回异常: HTTP " + response.code());
            }
            String respBody = response.body() != null ? response.body().string() : "";
            return parse(respBody);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用活体检测服务失败 url={}", url, e);
            throw new IllegalStateException("活体检测服务调用失败(可能超时): " + e.getMessage(), e);
        }
    }

    private LivenessVerifyResult parse(String respBody) {
        Map<String, Object> map = JSON.parseObject(respBody, Map.class);
        LivenessVerifyResult result = new LivenessVerifyResult();
        result.setLivenessScore(toBigDecimal(map.get("livenessScore")));
        result.setFaceMatchScore(toBigDecimal(map.get("faceMatchScore")));
        Object passed = map.get("livenessPassed");
        result.setLivenessPassed(Boolean.TRUE.equals(passed) || "true".equals(String.valueOf(passed)));
        Object attack = map.get("attackDetected");
        result.setAttackDetected(Boolean.TRUE.equals(attack) || "true".equals(String.valueOf(attack)));
        result.setAttackType(map.get("attackType") == null ? null : String.valueOf(map.get("attackType")));
        result.setMessage(map.get("message") == null ? null : String.valueOf(map.get("message")));
        return result;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(v));
    }
}
