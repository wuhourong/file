package com.danong.info.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.danong.info.service.WeatherService;
import com.danong.manage.pojo.Area;
import com.danong.manage.service.AreaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * <p>
 * 版权所有:(C)2015-2016 陈荣安参股公司
 * </p>
 * 
 * @作者: 陈荣安
 * @日期: 2016年5月17日 下午4:52:22
 * @描述: [WeatherController]天气查询(数据来源于外部数据仓库)
 */
@RequestMapping("info/weather")
@Controller
public class WeatherController {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired
	private WeatherService weatherService;

	@Autowired
	private AreaService areaService;

	@Value("${JISHU_AREA_LIST}")
	private String AREA_LIST;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * 
	 * <p>
	 * 方法名称: getWeatherById|描述:按城市ID或城市名查询天气
	 * </p>
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "queryWeatherById", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryWeatherById(@RequestParam(value = "id", defaultValue = "76") Long id) {
		try {
			JsonNode jsonNode = this.weatherService.queryWeatherRedis(id);
			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("按parentId查询省市行政地区信息出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 插入天气城市列表全量信息（此接口要慎重使用）每次都是全新插入
	 */
	@RequestMapping(value = "insertAllCity", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> insertAllCity() {
		try {
			this.weatherService.insertAllCity();

			// 200
			return ResponseEntity.ok(null);
		} catch (Exception e) {
			LOGGER.error("插入天气城市列表全量信息-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 
	 * <p>
	 * 方法名称: insertAllArea|描述: 插入省市行政地区信息全量信息（此接口要慎重使用）每次都是全新插入
	 * </p>
	 * 
	 * @return
	 */
	@RequestMapping(value = "insertAllArea", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> insertAllArea() {
		try {
			JsonNode jsonNode = MAPPER.readTree(AREA_LIST);
			Area area = null;

			JsonNode resultNode = jsonNode.get("result");

			if (resultNode.isArray()) {
				for (JsonNode objNode : resultNode) {
					area = MAPPER.readValue(objNode.toString(), Area.class);// 把Json串直接转成JAVA对象
					this.areaService.insertArea(area);
				}
			}

			// 200
			return ResponseEntity.ok(null);
		} catch (Exception e) {
			LOGGER.error("插入省市行政地区信息全量信息-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

}
