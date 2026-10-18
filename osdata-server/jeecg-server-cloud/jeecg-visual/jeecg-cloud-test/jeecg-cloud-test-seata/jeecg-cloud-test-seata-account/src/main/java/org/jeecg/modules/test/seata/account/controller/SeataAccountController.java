package org.jeecg.modules.test.seata.account.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.test.seata.account.service.SeataAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author zyf
 */
@RestController
@RequestMapping("/test/seata/account")
public class SeataAccountController {

    @Autowired
    private SeataAccountService accountService;

    @PostMapping("/reduceBalance")
    public Result<?> reduceBalance(Long userId, BigDecimal amount) {
        return accountService.reduceBalance(userId, amount);
    }
}
