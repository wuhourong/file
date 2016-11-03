package com.danong.info.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.danong.common.constants.RedisKeyConstants;
import com.danong.common.service.ApiService;
import com.danong.common.service.RedisService;
import com.danong.manage.mapper.WeatherCityMapper;
import com.danong.manage.pojo.WeatherCity;
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
 * @描述: [WeatherService]从外部数据仓库查询天气信息
 */
@Service
public class WeatherService {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired
	private ApiService apiService;

	@Autowired(required = true)
	private RedisService redisService;

	@Value("${JISHU_KEY}")
	private String JISHU_KEY;

	@Value("${JISHU_WEATHER_CITY_LIST}")
	private String WEATHER_CITY_LIST;

	@Value("${JISHU_WEATHER_GET_URL}")
	private String WEATHER_GET_URL;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private WeatherCityMapper weatherCityMapper;

	/**
	 * 
	 * <p>
	 * 方法名称: queryWeather|描述: 查询某城市当天天气(不支持省级查询，建议一般在市级，更低层级可能查不到)
	 * </p>
	 * 
	 * @param id
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryWeather(Long id) throws Exception {
		String wUrl = WEATHER_GET_URL + "?appkey=" + JISHU_KEY + "&cityid=" + id;
		String jsonData = this.apiService.doGet(wUrl);
		if (StringUtils.isEmpty(jsonData)) {
			LOGGER.error("查询当天天气-失败");
			return null;
		}
		JsonNode jsonNode = MAPPER.readTree(jsonData);
		LOGGER.info("查询当天天气-成功");
		/**
		 * 请谢斯清在这里将当天天气内容按城市ID为KEY存入Redis中，生存时间为4小时
		 */
		return jsonNode;
	}

	/**
	 * 从redis 获取 城市天气
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryWeatherRedis(Long id) throws Exception {
		JsonNode jsonNode = null;
		try {
			// 先从缓存中命中，如果命中的话返回，没有命中，程序继续执行
			String cacheData = this.redisService.get(RedisKeyConstants.REDIS_KEY_WEATHER + id);
			if (null != cacheData) {
				jsonNode = MAPPER.readTree(cacheData);
				return jsonNode;
			}

		} catch (Exception e) {
			LOGGER.error("从Redis中获取KEY为【" + RedisKeyConstants.REDIS_KEY_WEATHER + id + "】的操作出错了", e);
		}
		// 缓存没有命中时去数据仓库获取信息
		jsonNode = queryWeather(id);
		try {
			// 数据库查询后将结果集写入到缓存中
			this.redisService.set(RedisKeyConstants.REDIS_KEY_WEATHER + id, MAPPER.writeValueAsString(jsonNode), RedisKeyConstants.REDIS_TIME_WEATHER);
		} catch (Exception e) {
			LOGGER.error("将KEY为【" + RedisKeyConstants.REDIS_KEY_WEATHER + id + "】的数据写入Redis中出错", e);
		}

		return jsonNode;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: insertAllCity|描述: 把外部数据仓库中的天气城市列表信息同步到本地数据库中,每次都是全新插入
	 * </p>
	 * 
	 * @param flag
	 */
	public void insertAllCity() throws Exception {
		JsonNode jsonNode = MAPPER.readTree(WEATHER_GET_URL);
		try {
			WeatherCity weatherCity = null;

			JsonNode resultNode = jsonNode.get("result");

			if (resultNode.isArray()) {
				for (JsonNode objNode : resultNode) {
					weatherCity = MAPPER.readValue(objNode.toString(), WeatherCity.class);// 把Json串直接转成JAVA对象
					this.weatherCityMapper.insertSelective(weatherCity);
				}
			}
			LOGGER.info("把外部数据仓库中的省市行政地区信息同步到本地数据库中-成功,一共插入数据：" + resultNode.size() + "条");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
