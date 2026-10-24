package com.xfjr.consumer.realname.verify;

/**
 * 活体/人脸核验算法抽象（可插拔）。
 * 生产环境由 HttpLivenessVerifier 对接第三方活体检测厂商；
 * 决策逻辑（阈值/状态机）只依赖本接口，便于单测与替换实现。
 */
public interface LivenessVerifier {
    LivenessVerifyResult verify(LivenessVerifyRequest request);
}
