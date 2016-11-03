package com.danong.info.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

import com.danong.common.bean.JsonResult;
import com.danong.common.constants.CommonStatus;
import com.danong.info.service.IllegalInfoService;
import com.danong.manage.pojo.Illegal;
import com.danong.manage.pojo.IllegalCar;
import com.danong.manage.pojo.IllegalRecord;
import com.danong.manage.pojo.vo.IllegalCarVo;
import com.danong.manage.pojo.vo.IllegalVo;
import com.danong.manage.pojo.vo.UserExtra;
import com.danong.manage.service.IllegalCarService;
import com.danong.manage.service.IllegalService;
import com.danong.manage.service.UserService;
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
 * @描述: [IllegalController]违章查询(数据来源于外部数据仓库)
 */
@RequestMapping("info/illegal")
@Controller
public class IllegalInfoController {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private IllegalInfoService illegalInfoService;

	@Autowired
	private IllegalService illegalService;

	@Autowired
	private IllegalCarService illegalCarService;

	@Autowired
	private UserService userService;

	@Value("${USER_CAR_MAX_COUNT}")
	private String USER_CAR_MAX_COUNT;// 用户最多保存车辆个数

	/**
	 * 查询车牌类型列表
	 */
	@RequestMapping(value = "queryAllLsType", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryAllLsType() {
		try {
			JsonNode jsonNode = this.illegalInfoService.queryAllLsType();

			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("查询车牌类型列表-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 
	 * <p>
	 * 方法名称: queryAllLsprefix|描述:查询所有的省份缩写的列表(配置文件读取方案)
	 * </p>
	 * 
	 * @return
	 */
	@RequestMapping(value = "queryAllPrefix", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryAllPrefix() {
		try {
			JsonNode jsonNode = this.illegalInfoService.queryAllPrefix();
			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("查询所有的省份缩写的列表-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 查询违章所有省份
	 * 
	 * @return
	 */
	@RequestMapping(value = "queryAllIllegalProvince", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryAllIllegalProvince() {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			List<IllegalVo> list = this.illegalService.queryAllIllegalProvince();
			result.put("status", 0);
			result.put("msg", "ok");
			result.put("result", list);
			String str = MAPPER.writeValueAsString(result);
			JsonNode jsonNode = MAPPER.readTree(str);
			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("queryAllIllegalProvince异常", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 根据省份缩写查询所有的地市名称的列表
	 */
	@RequestMapping(value = "queryAllIllegalCityByPrefix", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryAllIllegalCityByPrefix(@RequestParam(value = "lsprefix", defaultValue = "粤") String lsprefix) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			List<Illegal> list = this.illegalService.queryCityByLsprefix(lsprefix);
			result.put("status", 0);
			result.put("msg", "ok");
			result.put("result", list);
			String str = MAPPER.writeValueAsString(result);
			JsonNode jsonNode = MAPPER.readTree(str);
			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("queryAllIllegalCityByPrefix异常", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 查询管局热门城市
	 * 
	 * @return
	 */
	@RequestMapping(value = "queryIllegalHotCity", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult queryIllegalHotCity() {
		try {
			List<Illegal> list = this.illegalService.queryIllegalHotCity();
			return new JsonResult(CommonStatus.COMMON_SUCCESS.value(), list, CommonStatus.COMMON_SUCCESS.getReason());
		} catch (Exception e) {
			LOGGER.error("queryWeatherHotCity异常", e);
		}
		// 错误，500
		return new JsonResult(CommonStatus.COMMON_FAIL.value(), CommonStatus.COMMON_FAIL.getReason());
	}

	/**
	 * 查询用户最多保存车辆个数
	 * 
	 * @return
	 */
	@RequestMapping(value = "queryUserCarMaxCount", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult queryUserCarMaxCount() {
		try {
			Integer count = Integer.parseInt(this.USER_CAR_MAX_COUNT);
			return new JsonResult(CommonStatus.COMMON_SUCCESS.value(), count, CommonStatus.COMMON_SUCCESS.getReason());
		} catch (Exception e) {
			LOGGER.error("queryUserCarMaxCount出错", e);
		}
		return new JsonResult(CommonStatus.COMMON_FAIL.value(), CommonStatus.COMMON_FAIL.getReason());
	}

	/**
	 * 方法名称: query|描述:根据车牌等信息查询具体违章信息
	 * 
	 * @param lsNum
	 *            车牌号
	 * @param engineStr
	 *            发动机号
	 * @param frameStr
	 *            车架号
	 * @param lstype
	 *            车牌类型
	 * @param cityIds
	 *            城市id
	 * @return
	 */
	@RequestMapping(value = "query", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult query(String token, String remark, @RequestParam(value = "lsNum", defaultValue = "贵A5FE92") String lsNum, @RequestParam(value = "engineStr", defaultValue = "FC5N048911") String engineStr, @RequestParam(value = "frameStr", defaultValue = "LS5AGE1FA021283") String frameStr, @RequestParam(value = "lstype", defaultValue = "02") String lstype, @RequestParam(value = "cityIds", defaultValue = "76") String cityIds, @RequestParam(value = "cityNames", defaultValue = "贵阳") String cityNames) {
		Map<Illegal, JsonNode> map = new HashMap<Illegal, JsonNode>();// 存储 管局 和对应 查询信息
		List<IllegalCarVo> illegalCarVoList; // 用户 车辆违章信息
		try {
			String[] ids = cityIds.split(",");
			if (ids.length > 3) {
				return new JsonResult(CommonStatus.ILLEGAL_CITY_NUM_LIMIT.value(), CommonStatus.ILLEGAL_CITY_NUM_LIMIT.getReason());
			}
			// 登录用户
			if (StringUtils.isNotBlank(token)) {
				UserExtra userExtra = userService.queryUserByToken(token);
				if (userExtra != null && userExtra.getUser() != null) {
					Long userId = userExtra.getUser().getId();// 用户id
					List<IllegalCar> carList = illegalCarService.queryIllegalCarListByUserIdAndName(userId, lsNum, 1);// 根据车牌号查询用户 车辆信息
					// 如果该车牌号之前没保存
					if (carList.isEmpty()) {
						int carCount = illegalCarService.queryIllegalCarCountByUserId(userId, 1);// 查询用户车辆数量
						if (carCount >= Integer.parseInt(this.USER_CAR_MAX_COUNT)) {
							// 如果用户已经保存超过两个车返回
							return new JsonResult(CommonStatus.ILLEGAL_CAR_NUM_LIMIT.value(), CommonStatus.ILLEGAL_CAR_NUM_LIMIT.getReason());
						}
					}

					// 保存或者更新用户车辆信息
					IllegalCar illegalCar = this.illegalInfoService.saveOrUpdateUserIllegalCar(carList, userId, lsNum, engineStr, frameStr, lstype, remark, cityIds, cityNames);

					// 循环查询交管局违章记录
					for (int i = 0; i < ids.length; i++) {
						Illegal illegal = this.illegalService.queryIllegalById(Long.parseLong(ids[i]));// 管局
						if (null != illegal) {
							JsonNode jsonNode = this.illegalInfoService.queryLsResult(lsNum, engineStr, frameStr, lstype, illegal);// 查询违章结果
							map.put(illegal, jsonNode);// 将管局 和查询信息放入map
						}
					}

					// 保存违章查询 以及 查询结果
					Iterator<Map.Entry<Illegal, JsonNode>> entries = map.entrySet().iterator();
					while (entries.hasNext()) {
						Map.Entry<Illegal, JsonNode> entry = entries.next();
						Illegal illegal = entry.getKey();// 管局
						JsonNode jsonNode = entry.getValue();// 查询信息
						this.illegalInfoService.saveUserIllegalInfo(jsonNode, illegal, illegalCar);// 保存用户违章查询和结果数据

					}
					illegalCarVoList = this.illegalCarService.queryIllegalCarListVoByUserId(userId, illegalCar, 1);// 查询用户 车辆违章信息
					return new JsonResult(CommonStatus.COMMON_SUCCESS.value(), illegalCarVoList, CommonStatus.COMMON_SUCCESS.getReason());
				}
			}

			// 未登录用户
			// 循环查询交管局违章记录
			for (int i = 0; i < ids.length; i++) {
				Illegal illegal = this.illegalService.queryIllegalById(Long.parseLong(ids[i]));// 管局
				if (null != illegal) {
					JsonNode jsonNode = this.illegalInfoService.queryLsResult(lsNum, engineStr, frameStr, lstype, illegal);// 查询违章结果
					map.put(illegal, jsonNode);// 将管局 和查询信息放入map
				}
			}

			List<IllegalRecord> illegalRecords = new ArrayList<IllegalRecord>();// 结果列表
			// 保存违章查询 以及 查询结果
			Iterator<Map.Entry<Illegal, JsonNode>> entries = map.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<Illegal, JsonNode> entry = entries.next();
				Illegal illegal = entry.getKey();// 管局
				JsonNode jsonNode = entry.getValue();// 查询信息
				this.illegalInfoService.queryIllegalInfo(jsonNode, illegal, illegalRecords);// 处理非登录用户查询数据
			}

			IllegalCarVo illegalCarVo = new IllegalCarVo();
			IllegalCar illegalCar = new IllegalCar();
			illegalCar.setLsnum(lsNum);// 车牌号
			illegalCar.setEngineStr(engineStr);
			illegalCar.setFrameStr(frameStr);
			illegalCar.setLstype(lstype);
			illegalCar.setRemark(remark);// 备注
			illegalCar.setCityIds(cityIds);
			illegalCar.setCityNames(cityNames);
			illegalCarVo.setIllegalCar(illegalCar);
			illegalCarVo.setIllegalRecords(illegalRecords);
			illegalCarVoList = new ArrayList<IllegalCarVo>();
			illegalCarVoList.add(illegalCarVo);
			return new JsonResult(CommonStatus.COMMON_SUCCESS.value(), illegalCarVoList, CommonStatus.COMMON_SUCCESS.getReason());
		} catch (Exception e) {
			LOGGER.error("查询车辆违章信息异常", e);
		}
		return new JsonResult(CommonStatus.COMMON_FAIL.value(), CommonStatus.COMMON_FAIL.getReason());
	}

	/**
	 * 更新车管局全量信息（此接口要慎重使用） flag=1时为更新，flag=0时为全新插入
	 */
	@RequestMapping(value = "updateCarorg", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> updateCarorg(@RequestParam(value = "flag", defaultValue = "1") int flag) {
		try {
			this.illegalInfoService.updateAllCarorg(flag);

			// 200
			return ResponseEntity.ok(null);
		} catch (Exception e) {
			LOGGER.error("更新车管局全量信息-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

}
