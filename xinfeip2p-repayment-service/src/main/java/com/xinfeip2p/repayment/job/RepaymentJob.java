package com.xinfeip2p.repayment.job;

import com.xinfeip2p.repayment.service.RepaymentService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 还款任务（@Scheduled + ShedLock 分布式锁，替代 ElasticJob）
 */
@Component
public class RepaymentJob {

    @Autowired
    private RepaymentService repaymentService;

    /**
     * 每天凌晨 1 点执行还款任务；ShedLock 保证集群内只有一个实例执行。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @SchedulerLock(name = "repaymentJobLock", lockAtLeastFor = "PT30S", lockAtMostFor = "PT10M")
    public void executeRepaymentJob() {
        // 调用业务层执行还款任务（单分片执行）
        repaymentService.executeRepayment(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), 1, 0);
        // 调用业务层执行还款短信提醒
        repaymentService.sendRepaymentNotify(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}
