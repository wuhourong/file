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

import com.danong.info.service.NewsService;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * <p>
 * 版权所有:(C)2015-2016 陈荣安参股公司
 * </p>
 * 
 * @作者: 陈荣安
 * @日期: 2016年5月17日 下午4:52:22
 * @描述: [NewsController]新闻浏览(数据来源于外部数据仓库)
 */
@RequestMapping("info/news")
@Controller
public class NewsController {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired
	private NewsService newsService;

	/**
	 * 根据频道名称查询新闻内容（暂定20条）
	 */
	@RequestMapping(value = "queryXinWenListByChannel", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryXinWenListByChannel(@RequestParam(value = "channel", defaultValue = "头条") String channel, @RequestParam(value = "num", defaultValue = "20") int num) {
		try {
			JsonNode jsonNode = this.newsService.queryXinWenListByChannelRedis(channel, num);

			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("根据频道名称查询新闻内容-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 查询新闻频道列表
	 */
	@RequestMapping(value = "getChannelList", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> getChannelList() {
		try {
			JsonNode jsonNode = this.newsService.getChannelList();

			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("根据搜索内容来查询新闻-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	/**
	 * 根据搜索内容来查询新闻（外部数据仓库提供的结果就10条）
	 */
	@RequestMapping(value = "queryXinWenByContent", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> queryXinWenByContent(@RequestParam String keyword) {
		try {
			JsonNode jsonNode = this.newsService.queryXinWenByContent(keyword);

			// 200
			return ResponseEntity.ok(jsonNode);
		} catch (Exception e) {
			LOGGER.error("根据搜索内容来查询新闻-出错", e);
		}
		// 错误，500
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

}
