package com.xfjr.depository.interceptor;





import com.xfjr.depository.common.util.EncryptUtil;


import com.xfjr.common.util.RSAUtil;


import com.xfjr.depository.common.util.ResponseUtil;


import com.xfjr.depository.entity.RequestDetails;


import com.xfjr.depository.service.RequestDetailsService;


import com.alibaba.fastjson2.JSON;


import com.alibaba.fastjson2.JSONObject;


import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.beans.factory.annotation.Value;


import org.springframework.stereotype.Component;


import org.springframework.web.servlet.HandlerInterceptor;





import jakarta.servlet.http.HttpServletRequest;


import jakarta.servlet.http.HttpServletResponse;





/**


 * <P>


 * 统一拦截P2P平台请求信息，并保存记录


 * </p>


 *


 * @author zhupeiyuan@itcast.cn


 * @since 2019-05-23


 */


@Slf4j


@Component


public class P2PRequestInterceptor implements HandlerInterceptor {





	@Value("${depository.privateKey}")


	private String depositoryPrivateKey;





	@Autowired


	private RequestDetailsService requestDetailsService;





	@Override


	public boolean preHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Object handler)


			throws Exception {


		String serviceName = httpRequest.getParameter("serviceName");


		String platformNo = httpRequest.getParameter("platformNo");


		String reqData = EncryptUtil.decodeUTF8StringBase64(httpRequest.getParameter("reqData"));


		JSONObject reqDataJSONObj = JSON.parseObject(reqData);


		String requestNo = reqDataJSONObj.getString("requestNo");





		RequestDetails requestDetails = requestDetailsService.getByRequestNo(requestNo);


		//如果请求为直连模式，且请求已被处理，直接返回处理结果


		if (requestDetails != null && httpRequest.getRequestURI().endsWith("/service")) {


			JSONObject responseJSON = new JSONObject();


			responseJSON.put("respData", JSON.parseObject(requestDetails.getResponseData()));


			responseJSON


					.put("signature", RSAUtil.sign(requestDetails.getResponseData(), depositoryPrivateKey, "utf-8"));


			ResponseUtil.responseOut(httpResponse, responseJSON.toJSONString());


			return false;


		}





		//保存请求信息


		requestDetails = new RequestDetails();


		requestDetails.setAppCode(platformNo);


		requestDetails.setServiceName(serviceName);


		requestDetails.setRequestNo(requestNo);


		requestDetails.setRequestData(reqData);


		requestDetailsService.create(requestDetails);





		return true;


	}





}


