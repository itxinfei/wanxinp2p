package com.xfjr.transaction.common.utils;

import com.xfjr.api.security.JwtUserContext;

/**
 * 获取当前登录用户。
 *
 * <p>身份来源为本服务校验过的 JWT 声明（{@link JwtUserContext}），不再解析外部透传的
 * {@code jsonToken} 参数（I-01）；再适配为交易域本地模型，保持既有调用点不变。
 */
public class SecurityUtil {

	/**
	 * 获取当前登录用户
	 */
	public static com.xfjr.transaction.model.LoginUser getUser() {
		com.xfjr.api.account.model.LoginUser source = JwtUserContext.getUser();
		com.xfjr.transaction.model.LoginUser target = new com.xfjr.transaction.model.LoginUser();
		target.setTenantId(source.getTenantId());
		target.setDepartmentId(source.getDepartmentId());
		target.setPayload(source.getPayload());
		target.setUsername(source.getUsername());
		target.setMobile(source.getMobile());
		target.setUserAuthorities(source.getUserAuthorities());
		target.setClientId(source.getClientId());
		return target;
	}

}
