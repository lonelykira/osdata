package org.jeecg.modules.test.seata.order.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;
import org.jeecg.modules.test.seata.order.entity.SeataOrder;
import org.jeecg.modules.test.seata.order.enums.OrderStatus;
import org.jeecg.modules.test.seata.order.feign.AccountClient;
import org.jeecg.modules.test.seata.order.feign.ProductClient;
import org.jeecg.modules.test.seata.order.mapper.SeataOrderMapper;
import org.jeecg.modules.test.seata.order.service.SeataOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Description: 订单服务类
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Service
public class SeataOrderServiceImpl implements SeataOrderService {

    @Resource
    private SeataOrderMapper orderMapper;
    @Resource
    private AccountClient accountClient;
    @Resource
    private ProductClient productClient;

    @DS("order")
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional
    public void placeOrder(PlaceOrderRequest request) {
        log.info("xid:"+RootContext.getXID());
        log.info("=============ORDER START=================");
        Long userId = request.getUserId();
        Long productId = request.getProductId();
        Integer count = request.getCount();
        log.info("收到下单请求,用户:{}, 商品:{},数量:{}", userId, productId, count);


        SeataOrder order = SeataOrder.builder()
                .userId(userId)
                .productId(productId)
                .status(OrderStatus.INIT)
                .count(count)
                .build();

        orderMapper.insert(order);
        log.info("订单一阶段生成，等待扣库存付款中");
        // 扣减库存并计算总价
        Result<BigDecimal> productRes = productClient.reduceStock(productId, count);
        if (!productRes.isSuccess()) {
            String message = productRes.getMessage();
            message = oConvertUtils.isEmpty(message) ? "操作失败" : message;
            throw new JeecgBootBizTipException(message);
        }
        BigDecimal amount = productRes.getResult();
        // 扣减余额
        Result<?> accountRes = accountClient.reduceBalance(userId, amount);
        // feign响应被二次封装，判断使主事务回滚
        if (!accountRes.isSuccess()) {
            String message = accountRes.getMessage();
            message = oConvertUtils.isEmpty(message) ? "操作失败" : message;
            throw new JeecgBootBizTipException(message);
        }

        order.setStatus(OrderStatus.SUCCESS);
        order.setTotalPrice(amount);
        orderMapper.updateById(order);
        log.info("订单已成功下单");
        log.info("=============ORDER END=================");
    }
}