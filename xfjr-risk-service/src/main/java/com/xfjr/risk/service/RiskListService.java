package com.xfjr.risk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.api.risk.RiskListDTO;
import com.xfjr.api.risk.RiskListRequest;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.risk.common.RiskErrorCode;
import com.xfjr.risk.entity.RiskList;
import com.xfjr.risk.mapper.RiskListMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 名单与额度管理（FR-122）：黑/白名单维护（手机号、身份证、设备、IP 维度），
 * 记录操作人与时间，即时生效（status=1 即参与决策引擎匹配）。
 */
@Service
public class RiskListService {

    private static final List<String> ALLOWED_DIMENSIONS = Arrays.asList("MOBILE", "ID_CARD", "DEVICE", "IP");

    private final RiskListMapper listMapper;

    public RiskListService(RiskListMapper listMapper) {
        this.listMapper = listMapper;
    }

    public List<RiskListDTO> list(String type) {
        LambdaQueryWrapper<RiskList> qw = new LambdaQueryWrapper<RiskList>().orderByDesc(RiskList::getId);
        if (type != null && !type.trim().isEmpty()) {
            String t = type.trim().toUpperCase();
            if (!"BLACK".equals(t) && !"WHITE".equals(t)) {
                throw new BusinessException(RiskErrorCode.E_180301, "名单类型仅支持 BLACK/WHITE: " + type);
            }
            qw.eq(RiskList::getListType, t);
        }
        List<RiskList> list = listMapper.selectList(qw);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public RiskListDTO add(RiskListRequest req) {
        if (req == null) {
            throw new BusinessException(RiskErrorCode.E_180301, "名单参数不能为空");
        }
        String t = req.getListType() == null ? "" : req.getListType().trim().toUpperCase();
        if (!"BLACK".equals(t) && !"WHITE".equals(t)) {
            throw new BusinessException(RiskErrorCode.E_180301, "名单类型仅支持 BLACK/WHITE: " + req.getListType());
        }
        if (req.getValue() == null || req.getValue().trim().isEmpty()) {
            throw new BusinessException(RiskErrorCode.E_180301, "名单值不能为空");
        }
        String dim = req.getDimension() == null ? "" : req.getDimension().trim().toUpperCase();
        if (!ALLOWED_DIMENSIONS.contains(dim)) {
            throw new BusinessException(RiskErrorCode.E_180303, "名单维度仅支持 MOBILE/ID_CARD/DEVICE/IP: " + req.getDimension());
        }
        LocalDateTime now = LocalDateTime.now();
        RiskList entity = new RiskList();
        entity.setListType(t);
        entity.setDimension(dim);
        entity.setValue(req.getValue().trim());
        entity.setReason(req.getReason());
        entity.setOperator(req.getOperator());
        entity.setStatus(1);
        entity.setCreateDate(now);
        entity.setUpdateDate(now);
        listMapper.insert(entity);
        return toDto(entity);
    }

    public void delete(Long id) {
        RiskList entity = listMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(RiskErrorCode.E_180302, "名单记录不存在: " + id);
        }
        listMapper.deleteById(id);
    }

    private RiskListDTO toDto(RiskList e) {
        RiskListDTO dto = new RiskListDTO();
        dto.setId(e.getId());
        dto.setListType(e.getListType());
        dto.setDimension(e.getDimension());
        dto.setValue(e.getValue());
        dto.setReason(e.getReason());
        dto.setOperator(e.getOperator());
        dto.setStatus(e.getStatus());
        dto.setCreateDate(e.getCreateDate());
        dto.setUpdateDate(e.getUpdateDate());
        return dto;
    }
}
