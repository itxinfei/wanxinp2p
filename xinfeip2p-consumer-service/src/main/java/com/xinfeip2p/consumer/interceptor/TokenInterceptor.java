package com.xinfeip2p.consumer.interceptor;

import com.xinfeip2p.api.account.model.LoginUser;
import com.xinfeip2p.common.util.EncryptUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Token拦截处理
 */
public class TokenInterceptor implements HandlerInterceptor {

	@Override  //前置拦截器
	public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
		String jsonToken = httpServletRequest.getParameter("jsonToken");
		if (StringUtils.isNotBlank(jsonToken)) {
			LoginUser loginUser = JSON
					.parseObject(EncryptUtil.decodeUTF8StringBase64(jsonToken), new TypeReference<LoginUser>() {
					});
			httpServletRequest.setAttribute("jsonToken", loginUser);
		}
		return true;
	}

}
