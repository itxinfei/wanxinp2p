package com.xfjr.repayment.interceptor;





import com.xfjr.common.util.EncryptUtil;


import com.xfjr.repayment.model.LoginUser;


import com.alibaba.fastjson2.JSON;


import com.alibaba.fastjson2.TypeReference;


import org.apache.commons.lang3.StringUtils;


import org.springframework.web.servlet.HandlerInterceptor;





import jakarta.servlet.http.HttpServletRequest;


import jakarta.servlet.http.HttpServletResponse;





/**


 * <P>


 * Token 拦截处理


 * </p>


 *


 * @author zhupeiyuan


 * @since 2019-05-09


 */


public class TokenInterceptor implements HandlerInterceptor {





	@Override


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


