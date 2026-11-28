package com.xfjr.transaction.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 交易库分片数据源（I-07 / DR-02）。
 *
 * <p>默认关闭：{@code xfjr.sharding.enabled=false} 时本配置不加载，服务走 Apollo 下发的单库数据源，行为与升级前一致。</p>
 *
 * <p>开启后按 {@code CONSUMER_ID % 2} 把交易数据路由到 {@code p2p_transaction_0/1}，
 * 分片表 {@code project/tender/claim} 各拆 {@code _0/_1}，三表互为绑定表。
 * 连接信息复用 Apollo 下发的 {@code spring.datasource.*}（仅替换库名）。</p>
 *
 * <p>注意：开启即全量分片读写；规划专题四的“灰度双写”属于后续阶段，不在本开关范围内。
 * 与 Seata AT 的联调、以及 undo_log 在分片库的落点需在启用前完成验证。</p>
 *
 * @author 心飞为你飞
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "xfjr.sharding", name = "enabled", havingValue = "true")
public class ShardingDataSourceConfiguration {

    /** 分片库名前缀（DR-02：CONSUMER_ID % 2）。 */
    private static final String DATABASE_NAME_PREFIX = "p2p_transaction_";

    /** 需要分片的表，顺序即绑定表声明顺序。 */
    private static final Collection<String> SHARDING_TABLES = Arrays.asList("project", "tender", "claim");

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    /**
     * 分片数据源（@Primary，覆盖 Apollo 下发的单库 DataSource）。
     */
    @Bean
    @Primary
    public DataSource dataSource() throws SQLException {
        log.info("交易库分片已启用：库 {}0/1，分片键 CONSUMER_ID % 2", DATABASE_NAME_PREFIX);
        Map<String, DataSource> dataSources = new LinkedHashMap<>(2);
        for (int i = 0; i < 2; i++) {
            dataSources.put("ds" + i, createTargetDataSource(i));
        }
        return ShardingSphereDataSourceFactory.createDataSource(dataSources,
                java.util.Collections.singletonList(shardingRuleConfig()), props());
    }

    /**
     * 按 {@code p2p_transaction_<i>} 替换单库连接串中的库名。
     */
    private DataSource createTargetDataSource(int index) {
        String targetUrl = switchDatabaseName(jdbcUrl, DATABASE_NAME_PREFIX + index);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(targetUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    private ShardingRuleConfiguration shardingRuleConfig() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();

        // 库分片：ds$->{CONSUMER_ID % 2}
        ruleConfig.getShardingAlgorithms().put("database_inline", inline("ds$->{CONSUMER_ID % 2}"));
        ruleConfig.setDefaultDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration("CONSUMER_ID", "database_inline"));

        // 表分片：<table_name>$->{CONSUMER_ID % 2}，并与库组合成实际节点 ds0/1.<table_name>_0/1
        for (String table : SHARDING_TABLES) {
            String lower = table.toLowerCase();
            ruleConfig.getShardingAlgorithms().put(lower + "_inline", inline(lower + "$->{CONSUMER_ID % 2}"));
            ShardingTableRuleConfiguration tableRule =
                    new ShardingTableRuleConfiguration(table, "ds$->{0..1}." + lower + "$->{0..1}");
            tableRule.setTableShardingStrategy(
                    new StandardShardingStrategyConfiguration("CONSUMER_ID", lower + "_inline"));
            ruleConfig.getTables().add(tableRule);
        }

        // 绑定表：避免笛卡尔路由
        ruleConfig.getBindingTableGroups()
                .add(new ShardingTableReferenceRuleConfiguration("transaction_group", String.join(",", SHARDING_TABLES)));
        return ruleConfig;
    }

    /** INLINE 分片算法（Groovy 表达式）。 */
    private AlgorithmConfiguration inline(String expression) {
        Properties props = new Properties();
        props.setProperty("algorithm-expression", expression);
        return new AlgorithmConfiguration("INLINE", props);
    }

    private Properties props() {
        Properties props = new Properties();
        // 分片路由日志，便于启用初期核对实际落库
        props.setProperty("sql-show", "true");
        return props;
    }

    /**
     * 把 jdbc:mysql://host:3306/p2p_transaction 改成指定库名，保留其余部分。
     */
    private String switchDatabaseName(String url, String databaseName) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("spring.datasource.url 未配置，无法启用交易库分片");
        }
        int slash = url.lastIndexOf('/');
        if (slash < 0) {
            return url;
        }
        String suffix = url.substring(slash + 1);
        // 库名后可能带 ?serverTimezone=... 参数，原样保留
        int question = suffix.indexOf('?');
        String params = question >= 0 ? suffix.substring(question) : "";
        return url.substring(0, slash + 1) + databaseName + params;
    }
}
