package com.xinfeip2p.uaa.controller;

import com.xinfeip2p.uaa.common.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 统一认证（页面视图控制器）
 * token 的签发/校验由 Spring Authorization Server 端点（/oauth2/token、/oauth2/introspect）提供。
 */
@Controller
public class UUAController {

    private static final Logger LOG = LoggerFactory.getLogger(UUAController.class);

    @RequestMapping(value = {"/login"})
    public String login(Model model) {
        LOG.info("Go to login, IP: {}", WebUtils.getIp());
        return "login";
    }

    @RequestMapping("/confirm_access")
    public String confirmAccess() {
        return "/oauth_approval";
    }

    @RequestMapping("/oauth_error")
    public String oauthError() {
        return "/oauth_error";
    }
}
