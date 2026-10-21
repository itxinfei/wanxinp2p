package cn.itcast.xinfeidepository.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "DepositoryResponse", description = "存管响应信息")
public class DepositoryResponse<T> {

    @Schema(description = "对respData参数的签名")
    private String signature;

    @Schema(description = "业务数据报文")
    private T respData;

	public DepositoryResponse(T respData) {
		this.respData = respData;
	}
}
