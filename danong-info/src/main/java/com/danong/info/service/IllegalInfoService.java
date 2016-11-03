package com.danong.info.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.danong.common.service.ApiService;
import com.danong.common.utils.DateUtil;
import com.danong.manage.mapper.IllegalMapper;
import com.danong.manage.pojo.Illegal;
import com.danong.manage.pojo.IllegalCar;
import com.danong.manage.pojo.IllegalQuery;
import com.danong.manage.pojo.IllegalRecord;
import com.danong.manage.pojo.vo.IllegalRecordVo;
import com.danong.manage.service.IllegalCarService;
import com.danong.manage.service.IllegalQueryService;
import com.danong.manage.service.IllegalRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.abel533.entity.Example;

/**
 * 
 * <p>
 * 版权所有:(C)2016-2018 达农保险
 * </p>
 * 
 * @作者: 陈荣安
 * @日期: 2016年8月9日 下午3:41:22
 * @描述: [IllegalService]从外部仓库拿到违章查询的相关数据
 */
@Service
public class IllegalInfoService {

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	@Autowired
	private ApiService apiService;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Value("${JISHU_KEY}")
	private String JISHU_KEY;

	@Value("${JISHU_ILLEGAL_LSTYPE_LIST}")
	private String ILLEGAL_LSTYPE_LIST;// 极速数据-违章查询-获取车牌类型

	@Value("${JISHU_ILLEGAL_CARORG_LIST}")
	private String ILLEGAL_CARORG_LIST;// 极速数据-违章查询-获取管局列表

	@Value("${JISHU_ILLEGAL_LSPREFIX_LIST}")
	private String JISHU_ILLEGAL_LSPREFIX_LIST;// 极速数据-违章查询-获取省份缩写列表

	@Value("${JISHU_ILLEGAL_GET_URL}")
	private String ILLEGAL_GET_URL;// 极速数据-违章查询-查询违章结果

	@Value("${ILLEGAL_USER_LIST}")
	private String ILLEGAL_USER_LIST;// 用户违章记录（测试数据）

	@Autowired
	private IllegalMapper illegalMapper;

	@Autowired
	private IllegalQueryService illegalQueryService;

	@Autowired
	private IllegalRecordService illegalRecordService;

	@Autowired
	private IllegalCarService illegalCarService;

	/**
	 * 
	 * <p>
	 * 方法名称: queryLstype|描述: 获取车牌类型列表
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryAllLsType() throws Exception {
		JsonNode jsonNode = MAPPER.readTree(ILLEGAL_LSTYPE_LIST);
		return jsonNode;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: queryAllLsprefix|描述: 查询所有的省份缩写的列表(配置文件读取方案)
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryAllPrefix() throws Exception {
		JsonNode jsonNode = MAPPER.readTree(JISHU_ILLEGAL_LSPREFIX_LIST);
		return jsonNode;
	}

	/**
	 * 查询用户违章测试数据
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryIllegalUserList() throws Exception {
		JsonNode jsonNode = MAPPER.readTree(ILLEGAL_USER_LIST);
		return jsonNode;
	}

	/**
	 * 
	 * <p>
	 * 方法名称: queryLsResult|描述: 查询违章结果
	 * </p>
	 * 
	 * @param lsNum
	 *            车牌号
	 * @param engineStr
	 *            发动机号
	 * @param frameStr
	 *            车架号
	 * @param lstype
	 *            车牌类型
	 * @param illegal
	 *            管局基础信息
	 * @return
	 * @throws Exception
	 */
	public JsonNode queryLsResult(String lsNum, String engineStr, String frameStr, String lstype, Illegal illegal) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(ILLEGAL_GET_URL + "?appkey=" + JISHU_KEY + "&carorg=" + illegal.getCarorg() + "&lsprefix=" + illegal.getLsprefix() + "&lsnum=" + StringUtils.substring(lsNum, 1) + "&lstype=" + lstype);
		if (0 == illegal.getFrameno()) {// 车架号为0时不用此参数

		} else if (100 == illegal.getFrameno()) {// 车架号为100时此参数必须完整
			url.append("&frameno=" + frameStr);
		} else {// 其它长度时则取最后N位长度字符串
			url.append("&frameno=" + StringUtils.substring(frameStr, frameStr.length() - illegal.getFrameno()));
		}

		if (0 == illegal.getEngineno()) {// 发动机号为0时不用此参数

		} else if (100 == illegal.getEngineno()) {// 发动机号为100时此参数必须完整
			url.append("&engineno=" + engineStr);
		} else {// 其它长度时则取最后N位长度字符串
			url.append("&engineno=" + StringUtils.substring(engineStr, engineStr.length() - illegal.getEngineno()));
		}

		String jsonData = this.apiService.doGet(url.toString());
		LOGGER.info("【违章查询--->请求外部地址】" + url.toString());
		JsonNode jsonNode = MAPPER.readTree(jsonData);
		// JsonNode jsonNode = this.queryIllegalUserList();// 查询用户违章测试数据
		/**
		 * 请谢斯清在这里加上处理： 1、通过判断jsonNode中result元素中的list元素是否有值，list中每一组值代表一条违章 2、如果是注册用户来查询，查到的结果有违章，则要把违章记录存入数据库。首页数据时，显示用户有几条违章时就使用这里记录好的数据，点击进来查询后才会真正的发起查询。 3、如果查到没有违章，则要把该注册用户数据库表里的记录清除掉。 4、另外对于已注册用户且登记了车辆数据信息的、应该建一个定时任务每天查询一次，并将结果记录到数据库中
		 */
		return jsonNode;
	}

	/**
	 * 保存用户车辆信息
	 * 
	 * @param userId
	 * @param lsNum
	 * @param remark
	 */
	public IllegalCar saveOrUpdateUserIllegalCar(List<IllegalCar> carList, Long userId, String lsNum, String engineStr, String frameStr, String lstype, String remark, String cityIds, String cityNames) {
		IllegalCar illegalCar = null;
		if (carList.isEmpty()) {
			illegalCar = new IllegalCar();
			illegalCar.setUserId(userId);// 用户id
			illegalCar.setLsnum(lsNum);// 车牌号全
			illegalCar.setEngineStr(engineStr);
			illegalCar.setFrameStr(frameStr);
			illegalCar.setLstype(lstype);
			illegalCar.setRemark(remark);// 车辆备注
			illegalCar.setCityIds(cityIds);
			illegalCar.setCityNames(cityNames);
			illegalCar.setState(1);
			illegalCarService.saveIllegalCar(illegalCar);// 保存用户车辆信息
		} else {
			illegalCar = carList.get(0);
			if (StringUtils.isNotBlank(remark)) {
				illegalCar.setRemark(remark);
			}
			illegalCar.setCityIds(cityIds);
			illegalCar.setCityNames(cityNames);
			illegalCarService.updateIllegalCar(illegalCar);// 更新用户车辆信息
		}

		return illegalCar;
	}

	/**
	 * 处理非登录用户查询数据
	 * 
	 * @param jsonNode
	 * @param illegal
	 * @param illegalRecords
	 * @throws Exception
	 */
	public void queryIllegalInfo(JsonNode jsonNode, Illegal illegal, List<IllegalRecord> illegalRecords) throws Exception {
		JsonNode jsonNodeResult = jsonNode.get("result");
		if (jsonNodeResult != null && StringUtils.isNotBlank(jsonNodeResult.toString()) && !StringUtils.equals("\"\"", jsonNodeResult.toString())) {
			JsonNode jsonNodeList = jsonNodeResult.get("list");
			if (jsonNodeList != null && StringUtils.isNotBlank(jsonNodeList.toString()) && !StringUtils.equals("\"\"", jsonNodeList.toString())) {
				// List<IllegalRecordVo> list = MAPPER.readValue(jsonNodeList.toString(), new TypeReference<List<IllegalRecordVo>>() {
				// });
				List<IllegalRecordVo> list = JSON.parseArray(jsonNodeList.toString(), IllegalRecordVo.class);
				for (IllegalRecordVo illegalRecordVo : list) {
					IllegalRecord illegalRecord = new IllegalRecord();
					illegalRecord.setIllegalId(illegal.getId());// 管局id
					illegalRecord.setCity(illegal.getCity());// 管局城市
					illegalRecord.setTime(DateUtil.parseDateHour(StringUtils.substring(illegalRecordVo.getTime(), 0, 19)));// 截取时间保证转换异常
					illegalRecord.setAddress(illegalRecordVo.getAddress());
					illegalRecord.setContent(illegalRecordVo.getContent());
					illegalRecord.setLegalnum(illegalRecordVo.getLegalnum());
					illegalRecord.setPrice(illegalRecordVo.getPrice());
					illegalRecord.setJsIllegalId(illegalRecordVo.getIllegalid());// 极速 数据违章id
					illegalRecord.setScore(illegalRecordVo.getScore());
					illegalRecords.add(illegalRecord);// 添加到违章记录列表
				}

			}

		}
	}

	/**
	 * 保存用户违章查询和结果数据
	 * 
	 * @param jsonNode
	 * @param illegal
	 * @param illegalCar
	 * @throws Exception
	 */
	public void saveUserIllegalInfo(JsonNode jsonNode, Illegal illegal, IllegalCar illegalCar) throws Exception {
		// 解析result节点数据(违章查询 如果为空表示没有查询 或者车辆信息不正确)
		JsonNode jsonNodeResult = jsonNode.get("result");
		// System.out.println(jsonNodeResult.textValue());
		// System.out.println(jsonNodeResult.toString());
		// System.out.println("\"\"".equals(jsonNodeResult.toString()));
		if (jsonNodeResult != null && StringUtils.isNotBlank(jsonNodeResult.toString()) && !StringUtils.equals("\"\"", jsonNodeResult.toString())) {
			IllegalQuery illegalQuery = new IllegalQuery();
			illegalQuery.setUserId(illegalCar.getUserId());// 用户id
			illegalQuery.setUserIllegalCarId(illegalCar.getId());// 用户车辆id
			illegalQuery.setIllegalId(illegal.getId());// 管局id
			illegalQuery.setLsprefix(jsonNodeResult.get("lsprefix").textValue());// 车牌前缀
			illegalQuery.setLsnum(jsonNodeResult.get("lsnum").textValue());// 车牌号
			illegalQuery.setCarorg(jsonNodeResult.get("carorg").textValue());// 管局名
			illegalQuery.setUsercarid(jsonNodeResult.get("usercarid").textValue());// 车辆id
			illegalQueryService.saveIllegalQuery(illegalQuery);// 保存违章查询

			// 查询车辆之前 在该管局 违章记录
			List<IllegalRecord> illegalRecordList = this.illegalRecordService.queryIllegalRecordListByUserId(illegalCar.getUserId(), illegalCar.getId(), illegal.getId(), 1);

			// 解析list节点数据(违章结果 如果 为空表示没有违章)
			JsonNode jsonNodeList = jsonNodeResult.get("list");
			if (jsonNodeList != null && StringUtils.isNotBlank(jsonNodeList.toString()) && !StringUtils.equals("\"\"", jsonNodeList.toString())) {
				// List<IllegalRecordVo> list = MAPPER.readValue(jsonNodeList.toString(), new TypeReference<List<IllegalRecordVo>>() {
				// });
				List<IllegalRecordVo> list = JSON.parseArray(jsonNodeList.toString(), IllegalRecordVo.class);
				// 如果查询到违章记录
				if (!list.isEmpty()) {
					for (IllegalRecordVo illegalRecordVo : list) {
						IllegalRecord illegalRecord = null;
						// 循环比较已经存在的违章记录 不存在 新增
						for (IllegalRecord pojo : illegalRecordList) {
							if (illegalRecordVo.getIllegalid().equals(pojo.getJsIllegalId())) {
								illegalRecord = pojo;
								break;
							}
						}
						// 如果数据库不存在该违章记录 新增
						if (illegalRecord == null) {
							illegalRecord = new IllegalRecord();
							illegalRecord.setUserId(illegalQuery.getUserId());// 用户id
							illegalRecord.setQueryId(illegalQuery.getId());// 查询id
							illegalRecord.setUserIllegalCarId(illegalCar.getId());// 用户车辆id
							illegalRecord.setIllegalId(illegal.getId());// 管局id
							illegalRecord.setCity(illegal.getCity());// 管局城市
							illegalRecord.setTime(DateUtil.parseDateHour(StringUtils.substring(illegalRecordVo.getTime(), 0, 19)));// 截取时间保证转换异常
							illegalRecord.setAddress(illegalRecordVo.getAddress());
							illegalRecord.setContent(illegalRecordVo.getContent());
							illegalRecord.setLegalnum(illegalRecordVo.getLegalnum());
							illegalRecord.setPrice(illegalRecordVo.getPrice());
							illegalRecord.setJsIllegalId(illegalRecordVo.getIllegalid());// 极速 数据违章id
							illegalRecord.setScore(illegalRecordVo.getScore());
							illegalRecord.setState(1);
							this.illegalRecordService.saveIllegalRecord(illegalRecord);// 保存违章记录
						}

					}
				} else {
					// 没有查到违章记录 如果之前有违章数据 清空之前违章数据
					for (IllegalRecord illegalRecord : illegalRecordList) {
						illegalRecord.setState(-1);
						illegalRecordService.updateIllegalRecord(illegalRecord);// 状态设置为已删除
					}
				}
			} else {
				// 没有查到违章记录 如果之前有违章数据 清空之前违章数据
				for (IllegalRecord illegalRecord : illegalRecordList) {
					illegalRecord.setState(-1);
					illegalRecordService.updateIllegalRecord(illegalRecord);// 状态设置为已删除
				}

			}

		} else {
			IllegalQuery illegalQuery = new IllegalQuery();
			illegalQuery.setUserId(illegalCar.getUserId());// 用户id
			illegalQuery.setUserIllegalCarId(illegalCar.getId());// 用户车辆id
			illegalQuery.setIllegalId(illegal.getId());// 管局id
			illegalQuery.setLsprefix(illegal.getLsprefix());// 车牌前缀
			illegalQuery.setLsnum(illegal.getLsnum());// 车牌号
			illegalQuery.setCarorg(illegal.getCarorg());// 管局名
			illegalQuery.setUsercarid(null);// 车辆id
			illegalQueryService.saveIllegalQuery(illegalQuery);// 保存违章查询
		}

	}

	/**
	 * 
	 * <p>
	 * 方法名称: updateAllCarorg|描述:把外部数据仓库中的管局列表信息同步到本地数据库中 flag=1时为更新，flag=0时为全新插入
	 * </p>
	 */
	public void updateAllCarorg(int flag) throws Exception {
		JsonNode jsonNode = MAPPER.readTree(ILLEGAL_CARORG_LIST);
		try {
			List<Illegal> illegalList = new ArrayList<Illegal>();
			Illegal illegal = null;

			JsonNode resultNode = jsonNode.get("result");

			JsonNode jData = resultNode.get("data");
			if (jData.isArray()) {
				for (JsonNode objNode : jData) {

					if (null != objNode.get("list")) {
						for (JsonNode objNode2 : objNode.get("list")) { // 如果省下面还有市，则继续循环
							illegal = new Illegal();

							illegal.setProvince(objNode.get("province").asText());// 省
							illegal.setLsprefix(objNode.get("lsprefix").asText());// 省的缩写

							illegal.setCity(objNode2.get("city").asText());// 市

							if (null == objNode2.get("carorg").asText() || "".equals(objNode2.get("carorg").asText())) {
								illegal.setCarorg(objNode.get("carorg").asText());// 如果市级管局名称为空，则用省级管局名称
							} else {
								illegal.setCarorg(objNode2.get("carorg").asText());// 否则直接用市级管局名称
							}

							if (null == objNode2.get("lsnum").asText() || "".equals(objNode2.get("lsnum").asText())) {
								illegal.setLsnum("$");// 如果车牌着字母为空，则直接使用“$”符号，代表任意字母均可
							} else {
								illegal.setLsnum(objNode2.get("lsnum").asText());// 否则直接用原来的 车牌首字母
							}

							if (null == objNode2.get("frameno").asText() || "".equals(objNode2.get("frameno").asText())) {
								illegal.setFrameno(0);
							} else {
								illegal.setFrameno(Integer.parseInt(objNode2.get("frameno").asText()));// 车架号需要输入的长度
																										// ，
																										// 100为全部输入
																										// 0为不输入
							}

							if (null == objNode2.get("engineno").asText() || "".equals(objNode2.get("engineno").asText())) {
								illegal.setEngineno(0);
							} else {
								illegal.setEngineno(Integer.parseInt(objNode2.get("engineno").asText()));// 发动机号需要输入的长度，
																											// 100为全部输入
																											// 0为不输入
							}

							if (0 == flag) {
								illegal.setCreated(new Date());
							}

							// illegal.setUpdated(new Date());//数据库会自动更新，无需程序处理

							illegalList.add(illegal);
						}
					} else { // 如果省下面没有市，则直接用省的数据来记录市级数据
						illegal = new Illegal();

						illegal.setProvince(objNode.get("province").asText());// 省
						illegal.setLsprefix(objNode.get("lsprefix").asText());// 省的缩写

						illegal.setCity(objNode.get("province").asText());// 市
						illegal.setCarorg(objNode.get("carorg").asText());// 管局名称
						illegal.setLsnum(objNode.get("lsnum").asText());// 车牌首字母

						if (null == objNode.get("frameno").asText() || "".equals(objNode.get("frameno").asText())) {
							illegal.setFrameno(0);
						} else {
							illegal.setFrameno(Integer.parseInt(objNode.get("frameno").asText()));// 车架号需要输入的长度
																									// ，
																									// 100为全部输入
																									// 0为不输入
						}

						if (null == objNode.get("engineno").asText() || "".equals(objNode.get("engineno").asText())) {
							illegal.setEngineno(0);
						} else {
							illegal.setEngineno(Integer.parseInt(objNode.get("engineno").asText()));// 发动机号需要输入的长度，
																									// 100为全部输入
																									// 0为不输入
						}

						if (null == objNode.get("lsnum").asText() || "".equals(objNode.get("lsnum").asText())) {
							illegal.setLsnum("$");// 如果车牌着字母为空，则直接使用“$”符号，代表任意字母均可
						} else {
							illegal.setLsnum(objNode.get("lsnum").asText());// 否则直接用原来的 车牌首字母
						}

						if (0 == flag) {
							illegal.setCreated(new Date());
						}
						// illegal.setUpdated(new Date());//数据库会自动更新，无需程序处理

						illegalList.add(illegal);
					}
				}
			}

			int uNum = 0;
			int iNum = 0;

			for (int i = 0; i < illegalList.size(); i++) {
				// 如果是插入则直接insert
				if (0 == flag) {
					illegalMapper.insertSelective(illegalList.get(i));
					iNum++;
				} else { // 否则就先查出ID来再更新

					Example example = new Example(Illegal.class);
					example.createCriteria().andEqualTo("city", illegalList.get(i).getCity());
					List<Illegal> list = illegalMapper.selectByExample(example);

					if (0 < list.size()) {
						illegalList.get(i).setId(list.get(0).getId());
						illegalMapper.updateByPrimaryKeySelective(illegalList.get(i));
						uNum++;
					} else {
						illegalMapper.insertSelective(illegalList.get(i));
						iNum++;
					}

				}
			}
			System.out.println("一共插入：" + iNum + "，更新：" + uNum + "，共：");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
