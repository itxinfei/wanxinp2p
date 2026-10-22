package com.xfjr.depository.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xfjr.depository.XfjrDepositoryServer;
import com.xfjr.depository.bank.BankCardNumberGenerator;
import com.xfjr.depository.bank.ChineseIDCardNumberGenerator;
import com.xfjr.depository.bank.ChineseMobileNumberGenerator;
import com.xfjr.depository.bank.ChineseNameGenerator;
import com.xfjr.depository.entity.BankCard;
import com.xfjr.depository.model.BankCardRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@ActiveProfiles("dev")
@SpringBootTest(classes = {XfjrDepositoryServer.class})
public class BankCardServiceTest {

    private static final Logger log = LoggerFactory.getLogger(BankCardServiceTest.class);

    @Autowired
    private BankCardService bankCardService;

    @Test
    public void testAddBankCard() {
        BankCardRequest bankCardRequest = new BankCardRequest();

        for (int i = 0; i < 100; i++) {
            bankCardRequest.setFullname(ChineseNameGenerator.getInstance().generate());
            bankCardRequest.setIdNumber(ChineseIDCardNumberGenerator.getInstance().generate());
            bankCardRequest.setMobile(ChineseMobileNumberGenerator.getInstance().generate());
            bankCardRequest.setBalance(new BigDecimal(100000));
            BankCard bankCard = BankCardNumberGenerator.getInstance().generateAll();
            BeanUtils.copyProperties(bankCard, bankCardRequest);
            bankCardRequest.setPassword("888888");
            bankCardRequest.setUserType(1);
            log.info(String.valueOf(bankCardRequest));
            bankCardService.createBankCard(bankCardRequest);
        }
    }

}
