package com.danong.info.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.danong.info.service.OilService;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * <p>
 * 版权所有:(C)2015-2016 陈荣安参股公司
 * </p>
 * 
 * @作者: 陈荣安
 * @日期: 2016年5月17日 下午4:52:22
 * @描述: [IllegalController]油价查询(数据来源于外部数据仓库)
 */
@RequestMapping("info/oil")
@Controller
public class OilController {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired
	private OilService oilService;

	/**
	 * 查询油价支持省市列表
	 */
	@RequestMapping(value = "getProvinceList", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> getProvinceList() {
		try {
			JsonNode jsonNode = this.oilService.getProvinceList();

			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("查询油价支持省市列表-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 按省市名称查询当时油价
	 */
	@RequestMapping(value = "queryOilByProvince", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryOilByProvince(@RequestParam(value = "province", defaultValue = "广东") String province) {
		try {
			JsonNode jsonNode = this.oilService.queryOilByProvinceRedis(province);
			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("按省市名称查询当时油价-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

}
