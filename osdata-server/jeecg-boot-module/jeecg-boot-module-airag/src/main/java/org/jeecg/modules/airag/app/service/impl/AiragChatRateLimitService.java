package org.jeecg.modules.airag.app.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * AI聊天匿名访问限流服务
 * 用于防止 /airag/chat/send、/airag/chat/upload 被恶意刷接口
 *
 * @author scott
 * @date 2026-07-20
 */
@Service
public class AiragChatRateLimitService {

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 每会话每分钟最多发送消息次数
	 */
	@Value("${jeecg.airag.rate-limit.send-per-session-per-minute:20}")
	private int sendPerSessionPerMinute;

	/**
	 * 每 IP 每分钟最多发送消息次数
	 */
	@Value("${jeecg.airag.rate-limit.send-per-ip-per-minute:60}")
	private int sendPerIpPerMinute;

	/**
	 * 每会话每小时最多上传文件次数
	 */
	@Value("${jeecg.airag.rate-limit.upload-per-session-per-hour:20}")
	private int uploadPerSessionPerHour;

	private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

	/**
	 * 校验 /airag/chat/send 调用频次
	 *
	 * @param request HttpServletRequest
	 */
	public void checkSendLimit(HttpServletRequest request) {
		String sessionId = request.getSession().getId();
		String clientIp = IpUtils.getIpAddr(request);
		String minute = LocalDateTime.now().format(MINUTE_FORMAT);

		checkLimit("airag:rate:send:session:" + sessionId + ":" + minute, sendPerSessionPerMinute, 120,
			"发送消息过于频繁，请稍后再试");
		checkLimit("airag:rate:send:ip:" + clientIp + ":" + minute, sendPerIpPerMinute, 120,
			"发送消息过于频繁，请稍后再试");
	}

	/**
	 * 校验 /airag/chat/upload 调用频次
	 *
	 * @param request HttpServletRequest
	 */
	public void checkUploadLimit(HttpServletRequest request) {
		String sessionId = request.getSession().getId();
		String clientIp = IpUtils.getIpAddr(request);
		String hour = LocalDateTime.now().format(HOUR_FORMAT);

		checkLimit("airag:rate:upload:session:" + sessionId + ":" + hour, uploadPerSessionPerHour, 7200,
			"上传文件过于频繁，请稍后再试");
		checkLimit("airag:rate:upload:ip:" + clientIp + ":" + hour, uploadPerSessionPerHour, 7200,
			"上传文件过于频繁，请稍后再试");
	}

	/**
	 * 固定窗口计数限流
	 *
	 * @param key Redis key
	 * @param limit 窗口上限
	 * @param expireSeconds key 过期时间（秒）
	 */
	private void checkLimit(String key, int limit, int expireSeconds, String errorMessage) {
		Long count = redisTemplate.opsForValue().increment(key, 1);
		if (count == null) {
			return;
		}
		if (count == 1) {
			redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
		}
		if (count > limit) {
			throw new JeecgBootException(errorMessage);
		}
	}
}
