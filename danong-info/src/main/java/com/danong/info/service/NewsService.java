package com.danong.info.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.danong.common.constants.RedisKeyConstants;
import com.danong.common.service.ApiService;
import com.danong.common.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * <p>
 * 版权所有:(C)2016-2018 达农保险
 * </p>
 * 
 * @作者: 陈荣安
 * @日期: 2016年8月9日 下午2:30:48
 * @描述: [XinWenService]从外部数据仓库提取到的新闻内容
 */
@Service
public class NewsService {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired(required = true)
	private RedisService redisService;

	@Autowired
	private ApiService apiService;

	@Value("${JISHU_KEY}")
	private String JISHU_KEY;

	@Value("${JISHU_NEWS_GET_URL}")
	private String NEWS_GET_URL;

	@Value("${JISHU_NEWS_SEARCH_URL}")
	private String NEWS_SEARCH_URL;

	@Value("${JISHU_NEWS_CHANNEL_LIST}")
	private String NEWS_CHANNEL_LIST;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * 
	 * <p>
	 * 方法名称: queryXinWenListByChannel|描述: 按频道名称查询新闻列表
	 * </p>
	 * 
	 * @param channel
	 *            频道名称
	 * @param num
	 *            新闻列表条数
	 * @return
	 */
	public JsonNode queryXinWenListByChannel(String channel, int num) throws Exception {
		String jsonData = this.apiService.doGet(NEWS_GET_URL + "?appkey=" + JISHU_KEY + "&channel=" + channel + "&num=" + num);
		if (StringUtils.isEmpty(jsonData)) {
			LOGGER.error("按频道名称查询新闻列表-失败");
			return null;
		}
		JsonNode jsonNode = MAPPER.readTree(jsonData);
		LOGGER.info("按频道名称查询新闻列表-成功");
		/**
		 * 请谢斯清在这里将新闻频道内容按频道名称为KEY存入Redis中，生存时间为4个小时
		 */
		return jsonNode;
	}

	/**
	 * 按频道名称查询新闻列表 redis
	 * 
	 * @param channel
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryXinWenListByChannelRedis(String channel, int num) throws Exception {
		JsonNode jsonNode = null;
		try {
			// 先从缓存中命中，如果命中的话返回，没有命中，程序继续执行
			String cacheData = this.redisService.get(RedisKeyConstants.REDIS_KEY_NEWS + channel);
			if (null != cacheData) {
				jsonNode = MAPPER.readTree(cacheData);
				return jsonNode;
			}

		} catch (Exception e) {
			LOGGER.error("从Redis中获取KEY为【" + RedisKeyConstants.REDIS_KEY_NEWS + channel + "】的操作出错了", e);
		}
		// 缓存没有命中时去数据仓库获取信息
		jsonNode = queryXinWenListByChannel(channel, num);
		try {
			// 数据库查询后将结果集写入到缓存中
			this.redisService.set(RedisKeyConstants.REDIS_KEY_NEWS + channel, MAPPER.writeValueAsString(jsonNode), RedisKeyConstants.REDIS_TIME_NEWS);
		} catch (Exception e) {
			LOGGER.error("将KEY为【" + RedisKeyConstants.REDIS_KEY_NEWS + channel + "】的数据写入Redis中出错", e);
		}

		return jsonNode;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: queryXinWenByConten|描述: 查询新闻频道列表(因为不常变化，所以直接写死)
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonNode getChannelList() throws Exception {
		JsonNode jsonNode = MAPPER.readTree(NEWS_CHANNEL_LIST);
		return jsonNode;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: queryXinWenByConten|描述: 按搜索内容来查询新闻内容
	 * </p>
	 * 
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryXinWenByContent(String keyword) throws Exception {
		String jsonData = this.apiService.doGet(NEWS_SEARCH_URL + "?appkey=" + JISHU_KEY + "&keyword=" + keyword);
		if (StringUtils.isEmpty(jsonData)) {
			LOGGER.error("按搜索内容来查询新闻内容-失败");
			return null;
		}
		JsonNode jsonNode = MAPPER.readTree(jsonData);
		LOGGER.info("按搜索内容来查询新闻内容-成功");
		return jsonNode;
	}

}
