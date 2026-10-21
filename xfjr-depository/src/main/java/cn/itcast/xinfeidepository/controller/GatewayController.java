package cn.itcast.xinfeidepository.controller;

import cn.itcast.xinfeidepository.common.util.CommonUtil;
import cn.itcast.xinfeidepository.common.util.EncryptUtil;
import cn.itcast.xinfeidepository.entity.BankCard;
import cn.itcast.xinfeidepository.entity.DepositoryBankCard;
import cn.itcast.xinfeidepository.model.PersonalRegisterRequest;
import cn.itcast.xinfeidepository.model.RechargeRequest;
import cn.itcast.xinfeidepository.model.WithdrawRequest;
import cn.itcast.xinfeidepository.service.BankCardService;
import cn.itcast.xinfeidepository.service.UserService;
import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 网关接口
 */
@Slf4j
@Controller
@Tag(name = "Gateway", description = "网关接口API")
public class GatewayController {

	@Autowired
	private BankCardService bankCardService;

	@Autowired
	private UserService userService;

	@Operation(summary = "开户")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@RequestMapping(value = "/gateway", method = RequestMethod.GET, params = "serviceName=PERSONAL_REGISTER")
	public ModelAndView create(@RequestParam("serviceName") String serviceName,
			@RequestParam("platformNo") String platformNo, @RequestParam("signature") String signature,
			@RequestParam("reqData") String reqData) {
		String decodeReqData = EncryptUtil.decodeUTF8StringBase64(reqData);
		PersonalRegisterRequest registerRequest = JSON.parseObject(decodeReqData, PersonalRegisterRequest.class);
		registerRequest.setAppCode(platformNo);
		registerRequest.setRole("B");
		//获取银行卡信息
		BankCard bankCard = bankCardService.getByCardNumber(registerRequest.getCardNumber());
		if (bankCard != null) {
			registerRequest.setBankName(bankCard.getBankName());
			registerRequest.setBankCode(bankCard.getBankCode());
		}
		log.debug("开户数据：{}", JSON.toJSONString(registerRequest));

		ModelAndView modelAndView = new ModelAndView("create");
		modelAndView.addObject("consumer", registerRequest);
		return modelAndView;
	}

	@Operation(summary = "充值")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@RequestMapping(value = "/gateway", method = RequestMethod.GET, params = "serviceName=RECHARGE")
	public ModelAndView recharge(@RequestParam("serviceName") String serviceName,
			@RequestParam("platformNo") String platformNo, @RequestParam("signature") String signature,
			@RequestParam("reqData") String reqData) {
		String decodeReqData = EncryptUtil.decodeUTF8StringBase64(reqData);
		RechargeRequest rechargeRequest = JSON.parseObject(decodeReqData, RechargeRequest.class);
		rechargeRequest.setAppCode(platformNo);

		//获取存管系统绑定银行卡信息
		DepositoryBankCard bankCard = userService.getDepositoryBankCardByUserNo(rechargeRequest.getUserNo());
		rechargeRequest.setBankCode(bankCard.getBankCode());
		rechargeRequest.setBankName(bankCard.getBankName());
		rechargeRequest.setCardNumber(bankCard.getCardNumber());
		rechargeRequest.setMobile(bankCard.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
		rechargeRequest.setMobile(CommonUtil.hiddenMobile(bankCard.getMobile()));
		log.debug("充值数据：{}", JSON.toJSONString(rechargeRequest));

		ModelAndView modelAndView = new ModelAndView("recharge");
		modelAndView.addObject("recharge", rechargeRequest);
		return modelAndView;
	}

	@Operation(summary = "提现")
	@Parameters({
			@Parameter(name = "serviceName", description = "接口名称", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "platformNo", description = "接入平台编号", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "signature", description = "针对请求数据reqData的签名", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "reqData", description = "业务数据报文，JSON 格式", required = true, in = ParameterIn.QUERY)})
	@RequestMapping(value = "/gateway", method = RequestMethod.GET, params = "serviceName=WITHDRAW")
	public ModelAndView withdraw(@RequestParam("serviceName") String serviceName,
			@RequestParam("platformNo") String platformNo, @RequestParam("signature") String signature,
			@RequestParam("reqData") String reqData) {

		String decodeReqData = EncryptUtil.decodeUTF8StringBase64(reqData);
		WithdrawRequest withdrawRequest = JSON.parseObject(decodeReqData, WithdrawRequest.class);
		withdrawRequest.setAppCode(platformNo);

		//获取存管系统绑定银行卡信息
		DepositoryBankCard bankCard = userService.getDepositoryBankCardByUserNo(withdrawRequest.getUserNo());
		withdrawRequest.setBankCode(bankCard.getBankCode());
		withdrawRequest.setBankName(bankCard.getBankName());
		withdrawRequest.setCardNumber(bankCard.getCardNumber());
		withdrawRequest.setMobile(bankCard.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));

		log.debug("提现数据：{}", JSON.toJSONString(withdrawRequest));

		ModelAndView modelAndView = new ModelAndView("withdraw");
		modelAndView.addObject("withdraw", withdrawRequest);
		return modelAndView;
	}

}