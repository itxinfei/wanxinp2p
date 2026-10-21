package cn.itcast.xinfeidepository.interceptor;





import cn.itcast.xinfeidepository.common.util.RSAUtil;


import cn.itcast.xinfeidepository.common.util.ResponseUtil;


import cn.itcast.xinfeidepository.entity.RequestDetails;


import cn.itcast.xinfeidepository.service.RequestDetailsService;


import com.alibaba.fastjson2.JSON;


import com.alibaba.fastjson2.JSONObject;


import lombok.extern.slf4j.Slf4j;


import org.apache.commons.lang3.StringUtils;


import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.beans.factory.annotation.Value;


import org.springframework.stereotype.Component;


import org.springframework.web.servlet.HandlerInterceptor;





import jakarta.servlet.http.HttpServletRequest;


import jakarta.servlet.http.HttpServletResponse;





/**


 * <P>


 * 统一拦截本地请求信息


 * </p>


 *


 * @author zhupeiyuan@itcast.cn


 * @since 2019-05-29


 */


@Slf4j


@Component


public class LocalRequestInterceptor implements HandlerInterceptor {





	@Value("${depository.privateKey}")


	private


	String depositoryPrivateKey;





	@Autowired


	private RequestDetailsService requestDetailsService;





	@Override


	public boolean preHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Object handler)


			throws Exception {


		RequestDetails requestDetails = requestDetailsService.getByRequestNo(httpRequest.getParameter("requestNo"));


		//请求已被处理，直接返回处理结果


		if (requestDetails != null && StringUtils.isNotBlank(requestDetails.getResponseData())) {


			JSONObject responseJSON = new JSONObject();


			responseJSON.put("respData", JSON.parseObject(requestDetails.getResponseData()));


			responseJSON


					.put("signature", RSAUtil.sign(requestDetails.getResponseData(), depositoryPrivateKey, "utf-8"));


			ResponseUtil.responseOut(httpResponse, responseJSON.toJSONString());


			return false;


		}





		return true;


	}





}


