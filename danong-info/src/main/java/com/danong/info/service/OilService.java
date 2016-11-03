package com.danong.info.service;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.danong.common.constants.RedisKeyConstants;
import com.danong.common.service.ApiService;
import com.danong.common.service.RedisService;
import com.danong.common.utils.DateUtil;
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
 * @描述: [OilService]从外部数据仓库提取到油价信息
 */
@Service
public class OilService {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired(required = true)
	private RedisService redisService;

	@Autowired
	private ApiService apiService;

	@Value("${JISHU_KEY}")
	private String JISHU_KEY;

	@Value("${JISHU_Oil_PROVINCE_LIST}")
	private String Oil_PROVINCE_LIST;

	@Value("${JISHU_Oil_GET}")
	private String Oil_GET;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * 
	 * <p>
	 * 方法名称: queryOil|描述: 按省市名称查询当天油价
	 * </p>
	 * 
	 * @param province
	 *            省市名称
	 * @return
	 */
	public JsonNode queryOilByProvince(String province) throws Exception {
		String jsonData = this.apiService.doGet(Oil_GET + "?appkey=" + JISHU_KEY + "&province=" + province);
		if (StringUtils.isEmpty(jsonData)) {
			LOGGER.error("查询" + province + "当天油价-失败");
			return null;
		}
		JsonNode jsonNode = MAPPER.readTree(jsonData);
		LOGGER.info("按省市名称查询当天油价-成功");
		/**
		 * 请谢斯清在这里将当天油价内容按省市名称为KEY存入Redis中，生存时间为当天24点前的剩余小时数
		 */
		return jsonNode;
	}

	/**
	 * 从redis 查询今日油价
	 * 
	 * @param province
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryOilByProvinceRedis(String province) throws Exception {
		JsonNode jsonNode = null;
		try {
			// 先从缓存中命中，如果命中的话返回，没有命中，程序继续执行
			String cacheData = this.redisService.get(RedisKeyConstants.REDIS_KEY_OIL + province);
			if (null != cacheData) {
				jsonNode = MAPPER.readTree(cacheData);
				return jsonNode;
			}

		} catch (Exception e) {
			LOGGER.error("从Redis中获取KEY为【" + RedisKeyConstants.REDIS_KEY_OIL + province + "】的操作出错了", e);
		}
		// 缓存没有命中时去数据仓库获取信息
		jsonNode = queryOilByProvince(province);
		try {
			Date now = new Date();
			Date end = DateUtil.parseDate(DateUtils.addDays(now, 1));
			int redisTime = DateUtil.getDateBetweenSeconds(new Date(), end);// 存储redis时间
			// 数据库查询后将结果集写入到缓存中
			this.redisService.set(RedisKeyConstants.REDIS_KEY_OIL + province, MAPPER.writeValueAsString(jsonNode), redisTime);
		} catch (Exception e) {
			LOGGER.error("将KEY为【" + RedisKeyConstants.REDIS_KEY_OIL + province + "】的数据写入Redis中出错", e);
		}

		return jsonNode;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: getProvinceList|描述: 查询油价支持省市列表(因为不常变化，所以直接写死)
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonNode getProvinceList() throws Exception {
		JsonNode jsonNode = MAPPER.readTree(Oil_PROVINCE_LIST);
		return jsonNode;
	}

}
