package com.danong.info.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.danong.common.bean.JsonResult;
import com.danong.common.constants.CommonStatus;
import com.danong.manage.pojo.vo.IllegalCarVo;
import com.danong.manage.pojo.vo.UserExtra;
import com.danong.manage.service.IllegalCarService;
import com.danong.manage.service.IllegalQueryService;
import com.danong.manage.service.IllegalRecordService;
import com.danong.manage.service.UserService;

/**
 * 用户违章结果
 * 
 */
@RequestMapping("info/illegal")
@Controller
public class IllegalUserController {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired
	private UserService userService;

	@Autowired
	private IllegalRecordService illegalRecordService;

	@Autowired
	private IllegalCarService illegalCarService;

	@Autowired
	private IllegalQueryService illegalQueryService;

	/**
	 * 查询用户 车辆违章结果记录
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "queryUserCarIllegalRecord", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult queryUserCarIllegalRecord(@RequestParam("token") String token) {
		try {
			UserExtra userExtra = userService.queryUserByToken(token);
			if (userExtra != null && userExtra.getUser() != null) {
				List<IllegalCarVo> list = this.illegalCarService.queryIllegalCarListVoByUserId(userExtra.getUser().getId(), 1);
				return new JsonResult(CommonStatus.COMMON_SUCCESS, list);
			} else {
				return new JsonResult(CommonStatus.TOKEN_TIME_OUT);
			}

		} catch (Exception e) {
			LOGGER.error("queryUserIllegalRecord出错", e);
		}
		return new JsonResult(CommonStatus.COMMON_FAIL);
	}

	/**
	 * 查询用户 违章结果记录 次数
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "queryUserCarIllegalRecordCount", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult queryUserCarIllegalRecordCount(@RequestParam("token") String token) {
		try {
			UserExtra userExtra = userService.queryUserByToken(token);
			if (userExtra != null && userExtra.getUser() != null) {
				Integer count = illegalRecordService.queryIllegalRecordCountByUserId(userExtra.getUser().getId(), 1);
				return new JsonResult(CommonStatus.COMMON_SUCCESS, count);
			} else {
				return new JsonResult(CommonStatus.TOKEN_TIME_OUT);
			}

		} catch (Exception e) {
			LOGGER.error("queryUserIllegalRecordCount出错", e);
		}
		return new JsonResult(CommonStatus.COMMON_FAIL);
	}

	/**
	 * 查询用户 违章查询记录 次数
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "queryUserCarIllegalQueryCount", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult queryUserCarIllegalQueryCount(@RequestParam("token") String token) {
		try {
			UserExtra userExtra = userService.queryUserByToken(token);
			if (userExtra != null && userExtra.getUser() != null) {
				Integer count = illegalQueryService.queryIllegalQueryCountByUserId(userExtra.getUser().getId());
				return new JsonResult(CommonStatus.COMMON_SUCCESS, count);
			} else {
				return new JsonResult(CommonStatus.TOKEN_TIME_OUT);
			}

		} catch (Exception e) {
			LOGGER.error("queryUserIllegalQueryCount出错", e);
		}
		return new JsonResult(CommonStatus.COMMON_FAIL);
	}

	/**
	 * 删除用户车辆信息
	 * 
	 * @param token
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "updateUserIllegalCar", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult updateUserIllegalCar(@RequestParam("token") String token, @RequestParam(value = "id", defaultValue = "0") Long id) {
		try {
			UserExtra userExtra = userService.queryUserByToken(token);
			if (userExtra != null && userExtra.getUser() != null) {
				this.illegalCarService.updateUserIllegalCarAndRecord(userExtra.getUser().getId(), id);
				return new JsonResult(CommonStatus.COMMON_SUCCESS);
			} else {
				return new JsonResult(CommonStatus.TOKEN_TIME_OUT);
			}
		} catch (Exception e) {
			LOGGER.error("updateUserIllegalCar出错", e);
		}
		return new JsonResult(CommonStatus.COMMON_FAIL);
	}

	/**
	 * 查询用户车辆数
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "queryIllegalCarCount", method = RequestMethod.GET)
	@ResponseBody
	public JsonResult queryIllegalCarCountByUserId(@RequestParam("token") String token) {
		try {
			LOGGER.info("【queryIllegalCarCount】token：" + token);
			UserExtra userExtra = userService.queryUserByToken(token);
			if (userExtra != null && userExtra.getUser() != null) {
				Integer count = this.illegalCarService.queryIllegalCarCountByUserId(userExtra.getUser().getId(), 1);
				return new JsonResult(CommonStatus.COMMON_SUCCESS, count);
			} else {
				return new JsonResult(CommonStatus.TOKEN_TIME_OUT);
			}
		} catch (Exception e) {
			LOGGER.error("queryIllegalCarCount出错", e);
		}
		return new JsonResult(CommonStatus.COMMON_FAIL);
	}
}
