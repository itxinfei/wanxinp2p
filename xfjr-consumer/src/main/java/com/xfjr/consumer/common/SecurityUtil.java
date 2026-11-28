package com.xfjr.consumer.common;

import com.xfjr.api.account.model.LoginUser;
import com.xfjr.api.security.JwtUserContext;

/**
 * 当前登录用户获取入口。
 *
 * <p>实现统一委托 {@link JwtUserContext}：用户身份来自本服务校验过的 JWT 声明，
 * 不再解析外部透传的 {@code jsonToken} 参数（I-01）。
 */
public class SecurityUtil {

	/**
	 * 获取当前登录用户
	 */
	public static LoginUser getUser() {
		return JwtUserContext.getUser();
	}

}
