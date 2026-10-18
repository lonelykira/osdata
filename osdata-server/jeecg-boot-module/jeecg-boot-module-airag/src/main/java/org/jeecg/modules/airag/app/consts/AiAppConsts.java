package org.jeecg.modules.airag.app.consts;

/**
 * AI应用常量类
 *
 * @author chenrui
 * @date 2025/2/25 14:52
 */
public class AiAppConsts {

    /**
     * 状态:启用
     */
    public static final String STATUS_ENABLE = "enable";
    /**
     * 状态:禁用
     */
    public static final String STATUS_DISABLE = "disable";
    /**
     * 状态:发布
     */
    public static final String STATUS_RELEASE = "release";


    /**
     * 默认应用id
     */
    public static final String DEFAULT_APP_ID = "default";

	/**
	 * 未保存应用的调试应用id
	 */
	public static final String DEBUG_APP_ID = "__DEBUG_APP";

    /**
     * 应用类型:简单聊天
     */
    public static final String APP_TYPE_CHAT_SIMPLE = "chatSimple";

    /**
     * 应用类型:聊天流(高级编排)
     */
    public static final String APP_TYPE_CHAT_FLOW = "chatFLow";

    /**
     * 应用元数据:流程输入参数
     * for [issues/8545]新建AI应用的时候只能选择没有自定义参数的AI流程
     */
    public static final String APP_METADATA_FLOW_INPUTS = "flowInputs";

    /**
     * 是否开启记忆
     */
    public static final Integer IZ_OPEN_MEMORY = 1;

    /**
     * 会话标题最大长度（AI总结失败时取用户提问前N字兜底，小程序/管理端历史列表展示）
     */
    public static final int CONVERSATION_MAX_TITLE_LENGTH = 30;

    /**
     * 会话来源：后台管理系统（按登录用户名作为唯一标识）
     */
    public static final String SESSION_SOURCE_MANAGE = "manage";

    /**
     * 会话来源：微信小程序（按openId作为唯一标识）
     */
    public static final String SESSION_SOURCE_WXAPP = "wxapp";

    /**
     * 小程序会话缓存key前缀（airag:chat:wxapp:{openId}，与管理后台用户会话空间隔离）
     */
    public static final String SESSION_DIR_PREFIX_WXAPP = "airag:chat:wxapp:";


    /**
     * AI写作的流程id
     */
    public static final String ARTICLE_WRITER_FLOW_ID = "2011769909807579138";

    /**
     * AI写作redis请求前缀
     */
    public static final String ARTICLE_WRITER_KEY = "airag:chat:article:write:{}";

    /**
     * ai绘画类型: 绘图
     */
    public static final String AI_DRAW_TYPE_DRAW = "draw";

    /**
     * ai绘画类型: 换脸
     */
    public static final String AI_DRAW_TYPE_FACE = "face";

    /**
     * ai绘画类型: 混图
     */
    public static final String AI_DRAW_TYPE_MIX = "mix";

    /**
     * ai绘画 会话redis请求前缀
     */
    public static final String POSTER_TASK_PREFIX = "airag:poster:task:";
    /** 任务结果在 Redis 中保留 1 小时 */
    public static final long POSTER_TASK_TTL = 3600L;
    
}
