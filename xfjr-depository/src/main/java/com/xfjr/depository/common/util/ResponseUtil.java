package com.xfjr.depository.common.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <P>
 * P2P平台请求返回数据工具类
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-24
 */
public class ResponseUtil {
    private static final Logger log = LoggerFactory.getLogger(ResponseUtil.class);


	/**
	 * 设置HttpResponse返回指定数据
	 * @param response
	 * @param s
	 */
	public static void responseOut(HttpServletResponse response, String s) {
		response.setContentType("application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		try (PrintWriter pw = response.getWriter()) {
			pw.write(s);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
