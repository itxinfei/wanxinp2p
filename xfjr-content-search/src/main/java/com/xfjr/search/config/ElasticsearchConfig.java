package com.xfjr.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 客户端配置（I-06：低级 REST 客户端，替代 RestHighLevelClient）。
 *
 * <p>地址来自 Apollo 命名空间 {@code micro_service.spring-boot-es} 的 {@code xfjr.es.host}，
 * 本地默认 {@code localhost:9200}。查询 DSL 与响应在业务层以 JSON 直构，不引入高级客户端依赖。
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${xfjr.es.host:localhost:9200}")
    private String esHost;

    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        String ip = esHost.split(":")[0];
        int port = Integer.parseInt(esHost.split(":")[1]);
        return RestClient.builder(new HttpHost(ip, port, "http")).build();
    }
}
