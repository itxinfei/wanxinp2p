package cn.itcast.xinfeidepository.controller;

import cn.itcast.xinfeidepository.common.constant.AuditStatusCode;
import cn.itcast.xinfeidepository.common.constant.TransactionStatusCode;
import cn.itcast.xinfeidepository.common.domain.BusinessException;
import cn.itcast.xinfeidepository.common.domain.PageVO;
import cn.itcast.xinfeidepository.common.util.EncryptUtil;
import cn.itcast.xinfeidepository.model.*;
import cn.itcast.xinfeidepository.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;

/**
 * <p>
 * 银行存管系统API
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-13
 */
@Slf4j
@RestController
@Tag(name = "Depository", description = "银行存管系统API")
public class DepositoryController {

	@Autowired
	private BankCardService bankCardService;

	@Autowired
	private UserService userService;

	@Autowired
	private BalanceDetailsService balanceDetailsService;

	@Autowired
	private RechargeDetailsService rechargeDetailsService;

	@Autowired
	private WithdrawDetailsService withdrawDetailsService;


	@Operation(summary = "获取手机验证码")
	@Parameter(name = "mobile", description = "手机号", required = true, in = ParameterIn.PATH)
	@GetMapping("/smscode/{mobile}")
	public LocalResponse<String> getSMSCode(@PathVariable String mobile) {
		return LocalResponse.success();
	}

	@Operation(summary = "银行卡开户")
	@PostMapping("/bank-cards")
	public LocalResponse<String> createBankCard(@RequestBody BankCardRequest bankCardRequest) {
		bankCardService.createBankCard(bankCardRequest);
		return LocalResponse.success();
	}

	@Operation(summary = "查询银行卡余额")
	@Parameter(name = "cardNumber", description = "卡号", required = true, in = ParameterIn.PATH)
	@GetMapping("/bank-cards/card-number/{cardNumber}")
	public LocalResponse<BigDecimal> getBalance(@PathVariable String cardNumber) {
		return LocalResponse.success(bankCardService.getBalance(cardNumber));
	}

	@Operation(summary = "获取用户余额信息")
	@Parameter(name = "userNo", description = "用户编码", required = true, in = ParameterIn.PATH)
	@GetMapping("/balance-details/{userNo}")
	public BalanceDetailsDTO getP2PBalanceDetails(@PathVariable String userNo) {
		BalanceDetailsDTO balanceDetailsDTO = balanceDetailsService.getP2PBalanceDetails(userNo);
		return balanceDetailsDTO;
	}

	@Operation(summary = "检索银行卡记录")
	@Parameters({
			@Parameter(name = "pageNo", description = "页码", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "pageSize", description = "每页记录数", required = true, in = ParameterIn.QUERY),
			@Parameter(name = "sortBy", description = "排序字段", in = ParameterIn.QUERY),
			@Parameter(name = "order", description = "顺序", in = ParameterIn.QUERY)})
	@PostMapping("/bank-cards/q")
	public LocalResponse<PageVO<BankCardDTO>> queryBankCards(@RequestBody BankCardQuery bankCardQuery,
			@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam String sortBy,
			@RequestParam String order) {
		PageVO<BankCardDTO> pageVO = bankCardService.queryBankCards(bankCardQuery, pageNo, pageSize, sortBy, order);
		return LocalResponse.success(pageVO);
	}


	@Operation(summary = "获取用户余额信息")
	@PostMapping("/users/password")
	public LocalResponse<Integer> verifyPassword(@RequestBody UserDTO userDTO) {
		return LocalResponse.success(userService.verifyPassword(userDTO.getUserNo(), userDTO.getPassword()) ? 1 : 0);
	}

	@Operation(summary = "用户开户")
	@PostMapping(value = "/trans/users")
	public ModelAndView createUser(PersonalRegisterRequest personalRegisterRequest) {
		int code;
		String msg = "";
		try {
			PersonalRegisterResponse response = userService.createUser(personalRegisterRequest);
			code = response.getAuditStatus().equals(AuditStatusCode.PASSED.getCode()) ? 0 : 1;
		} catch (BusinessException e) {
			code = 2;
			msg = e.getMessage();
		}
		return new ModelAndView(
				"redirect:" + addRedirectAttributes(personalRegisterRequest.getCallbackUrl(), code, msg));
	}

	@Operation(summary = "用户充值")
	@PostMapping(value = "/trans/recharge-details")
	public ModelAndView recharge(RechargeRequest rechargeRequest) {
		int code;
		String msg = "";
		try {
			RechargeResponse response = rechargeDetailsService.recharge(rechargeRequest);
			code = response.getTransactionStatus().equals(TransactionStatusCode.SUCCESS.getCode()) ? 0 : 1;
		} catch (BusinessException e) {
			code = 2;
			msg = e.getMessage();
		}
		return new ModelAndView("redirect:" + addRedirectAttributes(rechargeRequest.getCallbackUrl(), code, msg));
	}

	@Operation(summary = "用户提现")
	@PostMapping(value = "/trans/withdraw-details")
	public ModelAndView withdraw(WithdrawRequest withdrawRequest) {
		int code;
		String msg = "";
		try {
			WithdrawResponse response = withdrawDetailsService.withDraw(withdrawRequest);
			code = response.getTransactionStatus().equals(TransactionStatusCode.SUCCESS.getCode()) ? 0 : 1;
		} catch (BusinessException e) {
			code = 2;
			msg = e.getMessage();
		}
		return new ModelAndView("redirect:" + addRedirectAttributes(withdrawRequest.getCallbackUrl(), code, msg));
	}

	/**
	 * 添加url重定向参数
	 * @param url
	 * @param code
	 * @param msg
	 * @return
	 */
	private String addRedirectAttributes(String url, int code, String msg) {
		StringBuilder callbackUrl = new StringBuilder(url);
		callbackUrl.append("&code=").append(code);
		if (StringUtils.isNotBlank(msg)) {
			callbackUrl.append("&msg=").append(EncryptUtil.encodeURL(msg));
		}
		return callbackUrl.toString();
	}

}