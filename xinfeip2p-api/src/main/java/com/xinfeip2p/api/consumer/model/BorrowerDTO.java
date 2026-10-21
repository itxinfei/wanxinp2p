package com.xinfeip2p.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(name = "BorrowerDTO", description = "借款人用户信息")
public class BorrowerDTO {

	@Schema(description = "用户id")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "真实姓名")
	private String fullname;

	@Schema(description = "身份证号")
	private String idNumber;

	@Schema(description = "手机号")
	private String mobile;

	@Schema(description = "年龄")
	private Integer age;

	@Schema(description = "生日")
	private String birthday;

	@Schema(description = "性别")
	private String gender;

}