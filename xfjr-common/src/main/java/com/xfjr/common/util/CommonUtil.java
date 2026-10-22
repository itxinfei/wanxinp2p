package com.xfjr.common.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

/**
 * <P>
 * 通用工具类
 * </p>
 */
public class CommonUtil {
    private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);


	public static String hiddenMobile(String mobile) {
		if (StringUtils.isBlank(mobile)) {
			return "";
		}
		return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
	}

	public static void main(String[] args) {
		log.info(hiddenMobile("15378791911"));
	}

}

