package com.xfjr.consumer.controller;

import com.xfjr.common.domain.RestResponse;
import com.xfjr.consumer.member.model.LendRequest;
import com.xfjr.consumer.member.model.MemberInfoDTO;
import com.xfjr.consumer.member.model.MemberLevelDTO;
import com.xfjr.consumer.member.model.MemberLevelHistoryDTO;
import com.xfjr.consumer.member.model.PointChangeRequest;
import com.xfjr.consumer.member.model.PointRecordDTO;
import com.xfjr.consumer.member.service.MemberLevelService;
import com.xfjr.consumer.member.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员与积分（FR-127）。
 */
@RestController
@Slf4j
@RequestMapping("/l")
@Tag(name = "MemberPoint", description = "会员与积分API")
public class MemberPointController {

    @Autowired
    private MemberLevelService memberLevelService;
    @Autowired
    private PointService pointService;

    @Operation(summary = "会员等级配置列表")
    @GetMapping("/members/levels")
    public RestResponse<List<MemberLevelDTO>> listLevels() {
        return RestResponse.success(memberLevelService.listLevels());
    }

    @Operation(summary = "用户会员档案")
    @GetMapping("/members/{consumerNo}")
    public RestResponse<MemberInfoDTO> getMember(@PathVariable("consumerNo") String consumerNo) {
        return RestResponse.success(memberLevelService.getMemberInfo(consumerNo));
    }

    @Operation(summary = "等级变更历史")
    @GetMapping("/members/{consumerNo}/level-history")
    public RestResponse<List<MemberLevelHistoryDTO>> levelHistory(@PathVariable("consumerNo") String consumerNo) {
        return RestResponse.success(memberLevelService.getLevelHistory(consumerNo));
    }

    @Operation(summary = "出借完成：累计出借+自动升级+发放积分")
    @PostMapping("/members/{consumerNo}/lend")
    public RestResponse<MemberInfoDTO> lend(@PathVariable("consumerNo") String consumerNo, @RequestBody LendRequest req) {
        MemberInfoDTO info = memberLevelService.recordLend(consumerNo, req.getLendAmount());
        pointService.earnByLend(consumerNo, req.getLendAmount(), req.getBizNo());
        return RestResponse.success(info);
    }

    @Operation(summary = "发放积分")
    @PostMapping("/points/earn")
    public RestResponse<PointRecordDTO> earn(@RequestBody PointChangeRequest req) {
        return RestResponse.success(pointService.earn(req.getConsumerNo(), req.getBizType(), req.getAmount(), req.getBizNo(), req.getReason()));
    }

    @Operation(summary = "消耗积分（抵扣/兑换）")
    @PostMapping("/points/consume")
    public RestResponse<PointRecordDTO> consume(@RequestBody PointChangeRequest req) {
        return RestResponse.success(pointService.consume(req.getConsumerNo(), req.getBizType(), req.getAmount(), req.getBizNo(), req.getReason()));
    }

    @Operation(summary = "当前积分余额")
    @GetMapping("/points/{consumerNo}/balance")
    public RestResponse<Long> balance(@PathVariable("consumerNo") String consumerNo) {
        return RestResponse.success(pointService.currentBalance(consumerNo));
    }

    @Operation(summary = "积分流水")
    @GetMapping("/points/{consumerNo}/records")
    public RestResponse<List<PointRecordDTO>> records(@PathVariable("consumerNo") String consumerNo) {
        return RestResponse.success(pointService.listRecords(consumerNo));
    }
}
