package cn.itcast.xinfeidepository.service;



import cn.itcast.xinfeidepository.common.constant.StatusCode;

import cn.itcast.xinfeidepository.entity.RequestDetails;

import cn.itcast.xinfeidepository.mapper.RequestDetailsMapper;

import cn.itcast.xinfeidepository.model.BaseResponse;

import com.alibaba.fastjson2.JSON;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



/**

 * <p>

 * 存管系统请求信息表 服务实现类

 * </p>

 *

 * @author zhupeiyuan

 * @since 2019-05-23

 */

@Slf4j

@Service

public class RequestDetailsServiceImpl extends ServiceImpl<RequestDetailsMapper, RequestDetails>

		implements RequestDetailsService {



	@Override

	public RequestDetails create(RequestDetails requestDetails) {

		requestDetails.setStatus(StatusCode.STATUS_OUT.getCode());

		save(requestDetails);

		return requestDetails;

	}



	@Override

	public Boolean modifyByRequestNo(RequestDetails requestDetails) {

		return update(requestDetails, new QueryWrapper<RequestDetails>().lambda()

				.eq(RequestDetails::getRequestNo, requestDetails.getRequestNo()));

	}



	@Override

	public Boolean modifyGatewayByRequestNo(BaseResponse response) {

		RequestDetails requestDetails = new RequestDetails();

		requestDetails.setRequestNo(response.getRequestNo());

		requestDetails.setStatus(response.getStatus());

		requestDetails.setResponseData(JSON.toJSONString(response));

		return modifyByRequestNo(requestDetails);

	}



	@Override

	public RequestDetails getByRequestNo(String requestNo) {

		return getOne(new QueryWrapper<RequestDetails>().lambda().eq(RequestDetails::getRequestNo, requestNo), false);

	}

}

