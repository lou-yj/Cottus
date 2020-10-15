package com.louyj.cottus.server.automation;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.cottus.server.util.PlaceHolderUtils.replacePlaceHolder;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.louyj.cottus.server.automation.event.AlarmEvent;
import com.louyj.cottus.server.util.MatchUtils;
import com.louyj.cottus.server.util.PlaceHolderUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementStateListener;
import com.espertech.esper.client.annotation.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.DocumentContext;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.bean.automate.AlarmInhibitor;
import com.louyj.rhttptunnel.model.bean.automate.AlarmMarker;
import com.louyj.rhttptunnel.model.bean.automate.AlarmTrace;
import com.louyj.rhttptunnel.model.bean.automate.AlarmTriggeredRecord;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.rhttptunnel.model.bean.automate.HandlerProcessInfo;
import com.louyj.rhttptunnel.model.bean.automate.HandlerProcessInfo.HandlerExecuteInfo;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class AlarmService implements EPStatementStateListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final String uuid = UUID.randomUUID().toString();
	private int providerCounter = 0;

	private AtomicReference<EPRuntime> epRuntime = new AtomicReference<>();
	private EPServiceProvider epService;
	private String esperConfig;
	private HandlerService handlerService;
	private AutomateManager automateManager;
	private ObjectMapper jackson = JsonUtils.jackson();

	public AlarmService(HandlerService handlerService, AutomateManager automateManager) {
		super();
		this.handlerService = handlerService;
		this.automateManager = automateManager;
		try {
			URL resource = ClassLoader.getSystemClassLoader().getResource("esper.xml");
			if (resource == null) {
				resource = ClassLoader.getSystemClassLoader().getResource("BOOT-INF/classes/esper.xml");
			}
			esperConfig = IOUtils.toString(resource, UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public AtomicReference<EPRuntime> getEpRuntime() {
		return epRuntime;
	}

	public void sendEvent(Object event) {
		epRuntime.get().sendEvent(event);
	}

	public void sendEvent(String name, Map<String, Object> event) {
		epRuntime.get().sendEvent(event, name);
	}

	public AtomicReference<EPRuntime> resetEsper(List<Alarmer> alarmers) {
		try {
			String oldProviderURI = String.format("provider-%s-%d", uuid, providerCounter);
			String currentProviderURI = String.format("provider-%s-%d", uuid, ++providerCounter);
			try {
				epService = EPServiceProviderManager.getProvider(currentProviderURI, getConfiguration(esperConfig));
				epService.initialize();
				epService.addStatementStateListener(this);
				EPAdministrator admin = epService.getEPAdministrator();
				createStatement(admin, alarmers);
			} catch (Exception e) {
				epService.destroy();
				logger.error("Init esper provoder {} failed", currentProviderURI);
				providerCounter = providerCounter - 1;
				throw e;
			}
			epRuntime.set(epService.getEPRuntime());
			logger.info("Init esper provoder {}", currentProviderURI);
			EPServiceProvider oldProvider = EPServiceProviderManager.getExistingProvider(oldProviderURI);
			if (oldProvider != null) {
				oldProvider.destroy();
				logger.info("Destory esper provoder {}", oldProviderURI);
			}
			return epRuntime;
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onStatementCreate(EPServiceProvider serviceProvider, EPStatement statement) {
		try {
			logger.info("Create EPL: [{}].", statement.getText());
			Annotation[] annotations = statement.getAnnotations();
			if (annotations == null) {
				return;
			}
			for (Annotation annotation : annotations) {
				if (!(annotation instanceof Tag)) {
					continue;
				}
				Tag tagAnno = (Tag) annotation;
				String name = tagAnno.name();
				String value = tagAnno.value();
				if (name.equalsIgnoreCase("alarm") && value.equalsIgnoreCase("main")) {
					Alarmer alarmer = (Alarmer) statement.getUserObject();
					AlarmEventListener listener = new AlarmEventListener(alarmer, this);
					statement.addListener(listener);
					logger.info("Add Listener for rule: {}.", alarmer.getName());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onStatementStateChange(EPServiceProvider serviceProvider, EPStatement statement) {
	}

	public void destory() {
		try {
			epService.destroy();
		} catch (Exception e) {
		}
		logger.info("Alarm service destoryed.");
	}

	public void handleAlarm(AlarmEvent alarmEvent) {
		Map<String, Object> eventMap = alarmEvent.toMap();
		String uuid = automateManager.nextIndex();
		alarmEvent.setUuid(uuid);
		logger.info("[{}] Receive alarm event {}", uuid, JsonUtils.gson().toJson(eventMap));
		logger.info("[{}] Start mark alarm event", uuid);
		for (AlarmMarker alarmMarker : automateManager.getAlarmMarkers()) {
			eventMap = alarmEvent.toMap();
			DocumentContext eventDc = PlaceHolderUtils.toDc(eventMap);
			boolean matched = MatchUtils.isMatched(alarmMarker.isRegexMatch(), eventMap, alarmMarker.getMatched(),
					eventDc);
			if (matched) {
				logger.info("Marker {} condition matched ", alarmMarker.getName());
				if (MapUtils.isNotEmpty(alarmMarker.getProperties())) {
					Map<String, Object> env = Maps.newHashMap();
					env.putAll(eventMap);
					env.putAll(alarmMarker.getProperties());
					DocumentContext dc = PlaceHolderUtils.toDc(env);
					Map<String, Object> replacedTags = Maps.newHashMap();
					for (Entry<String, Object> entry : alarmMarker.getTags().entrySet()) {
						String key = PlaceHolderUtils.replacePlaceHolder(dc, entry.getKey());
						Object value = PlaceHolderUtils.replacePlaceHolder(dc, entry.getValue());
						replacedTags.put(key, value);
					}
					alarmEvent.getTags().add(Pair.of(alarmMarker.getName(), replacedTags));
				} else {
					alarmEvent.getTags().add(Pair.of(alarmMarker.getName(), Maps.newHashMap()));
				}
			}
		}
		logger.info("[{}] End mark alarm event", uuid);
		logger.info("[{}] Start eval silencers", uuid);
		eventMap = alarmEvent.toMap();
		DocumentContext eventDc = PlaceHolderUtils.toDc(eventMap);
		List<AlarmSilencer> alarmSilencers = findAvalilAlarmSilencers();
		for (AlarmSilencer alarmSilencer : alarmSilencers) {
			boolean matched = MatchUtils.isMatched(alarmSilencer.isRegexMatch(), eventMap, alarmSilencer.getMatched(),
					eventDc);
			if (matched) {
				logger.info("[{}] Matches silencer, match condition {} regex {} start time {} end time {}",
						alarmSilencer.getMatched(), alarmSilencer.isRegexMatch(),
						new DateTime(alarmSilencer.getStartTime()).toString("yyyy-MM-dd HH:mm:ss"),
						new DateTime(alarmSilencer.getEndTime()).toString("yyyy-MM-dd HH:mm:ss"));
				alarmEvent.setAlarmSilencerId(alarmSilencer.getUuid());
				break;
			}
		}
		logger.info("[{}] End eval silencers", uuid);
		automateManager.getAlarmCache().put(uuid, alarmEvent);
		if (alarmEvent.getAlarmSilencerId() != null) {
			return;
		}
		logger.info("[{}] Start eval inhibitors", uuid);
		for (AlarmInhibitor alarmInhibitor : automateManager.getAlarmInhibitors()) {
			boolean regexMatch = alarmInhibitor.isRegexMatch();
			boolean matched = MatchUtils.isMatched(regexMatch, eventMap, alarmInhibitor.getMatched(), eventDc);
			if (matched == false) {
				continue;
			}
			List<Map<String, Object>> latestEvent = searchLatestEvent(alarmInhibitor.getTimeWindowSize());
			boolean windowMatched = MatchUtils.isWindowMatched(regexMatch, latestEvent,
					alarmInhibitor.getWindowMatched(), eventDc);
			if (windowMatched == false) {
				continue;
			}
			logger.info("[{}] Alarm inhibited by inhibitor {}", alarmInhibitor.getName());
			alarmEvent.setAlarmInhibitor(alarmInhibitor);
			break;
		}
		logger.info("[{}] End eval inhibitors", uuid);
		if (alarmEvent.getAlarmInhibitor() != null) {
			return;
		}
		handlerService.handleAlarm(alarmEvent);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> searchLatestEvent(int timeWindowSize) {
		long timeDeadLine = System.currentTimeMillis() - timeWindowSize * 1000;
		SqlFieldsQuery sql = new SqlFieldsQuery("SELECT uuid,fields FROM AlarmEvent where alarmTime > ?")
				.setArgs(timeDeadLine);
		List<Map<String, Object>> result = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor = automateManager.getAlarmCache().query(sql)) {
			for (List<?> row : cursor) {
				Map<String, Object> windowMap = (Map<String, Object>) row.get(1);
				result.add(windowMap);
			}
		}
		return result;
	}

	public List<AlarmTriggeredRecord> searchAlarmRecords(String name, int limit) {
		SqlFieldsQuery sql = new SqlFieldsQuery(
				"SELECT uuid,alarmTime,alarmGroup,fields FROM AlarmEvent info where alarmRule=? order by alarmTime desc limit ?")
						.setArgs(name, limit);
		List<AlarmTriggeredRecord> result = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor = automateManager.getAlarmCache().query(sql)) {
			for (List<?> row : cursor) {
				AlarmTriggeredRecord record = new AlarmTriggeredRecord();
				int index = 0;
				record.setUuid(rowGet(row, index++));
				record.setAlarmTime(rowGet(row, index++));
				record.setAlarmGroup(rowGet(row, index++));
				record.setFields(rowGet(row, index++));
				result.add(record);
			}
		}
		return result;
	}

	public List<AlarmSilencer> findAvalilAlarmSilencers() {
		SqlFieldsQuery sql = new SqlFieldsQuery(
				"SELECT uuid,regexMatch,matched,startTime,endTime FROM AlarmSilencer info where CURRENT_TIMESTAMP(3) >= startTime and CURRENT_TIMESTAMP(3) <= endTime");
		List<AlarmSilencer> result = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor = automateManager.getAlarmCache().query(sql)) {
			for (List<?> row : cursor) {
				int index = 0;
				AlarmSilencer alarmSilencer = new AlarmSilencer();
				alarmSilencer.setUuid(rowGet(row, index++));
				alarmSilencer.setRegexMatch(rowGet(row, index++));
				alarmSilencer.setMatched(rowGet(row, index++));
				alarmSilencer.setStartTime(rowGet(row, index++));
				alarmSilencer.setEndTime(rowGet(row, index++));
				result.add(alarmSilencer);
			}
		}
		return result;
	}

	public AlarmTrace findAlarmTrace(String uuid) {
		AlarmTrace alarmTrace = new AlarmTrace();
		AlarmEvent alarmEvent = (AlarmEvent) automateManager.getAlarmCache().get(uuid);
		AlarmTriggeredRecord record = new AlarmTriggeredRecord();
		record.setUuid(alarmEvent.getUuid());
		record.setAlarmTime(alarmEvent.getAlarmTime());
		record.setAlarmGroup(alarmEvent.getAlarmGroup());
		record.setFields(alarmEvent.getFields());
		record.setTags(alarmEvent.getTags());
		alarmTrace.setRecord(record);

		String alarmSilencerId = alarmEvent.getAlarmSilencerId();
		if (alarmSilencerId != null) {
			AlarmSilencer alarmSilencer = (AlarmSilencer) automateManager.getAlarmCache().get(alarmSilencerId);
			com.louyj.rhttptunnel.model.bean.automate.AlarmSilencer alSilencer = jackson.convertValue(alarmSilencer,
					com.louyj.rhttptunnel.model.bean.automate.AlarmSilencer.class);
			alarmTrace.setAlarmSilencer(alSilencer);
		}
		alarmTrace.setAlarmInhibitor(alarmEvent.getAlarmInhibitor());

		SqlFieldsQuery sql = new SqlFieldsQuery(
				"SELECT alarmId,handlerId,evaluateTime,preventedBy,scheduledTime,params,targetHosts,scheduleId,status,message,correlationAlarmIds FROM AlarmHandlerInfo info where alarmId=? order by evaluateTime")
						.setArgs(uuid);
		List<HandlerProcessInfo> handlerInfos = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor = automateManager.getAlarmCache().query(sql)) {
			for (List<?> row : cursor) {
				int index = 0;
				HandlerProcessInfo pinfo = new HandlerProcessInfo();
				pinfo.setAlarmId(rowGet(row, index++));
				pinfo.setHandlerId(rowGet(row, index++));
				pinfo.setEvaluateTime(rowGet(row, index++));
				pinfo.setPreventedBy(rowGet(row, index++));
				pinfo.setScheduledTime(rowGet(row, index++));
				pinfo.setParams(rowGet(row, index++));
				pinfo.setTargetHosts(rowGet(row, index++));
				pinfo.setScheduleId(rowGet(row, index++));
				pinfo.setStatus(rowGet(row, index++));
				pinfo.setMessage(rowGet(row, index++));
				List<String> correlationAlarmIds = rowGet(row, index++);
				List<Map<String, Object>> correlationAlarms = Lists.newArrayList();
				for (String caid : correlationAlarmIds) {
					AlarmEvent ae = (AlarmEvent) automateManager.getAlarmCache().get(caid);
					correlationAlarms.add(ae.getFields());
				}
				pinfo.setCorrelationAlarms(correlationAlarms);
				handlerInfos.add(pinfo);
			}
		}
		alarmTrace.setHandlerInfos(handlerInfos);
		for (HandlerProcessInfo pinfo : handlerInfos) {
			sql = new SqlFieldsQuery(
					"SELECT sre,metrics,status,message,stdout,stderr FROM ScheduledTaskAudit audit where scheduleId=? order by time")
							.setArgs(pinfo.getScheduleId());
			List<HandlerExecuteInfo> executeInfos = Lists.newArrayList();
			try (QueryCursor<List<?>> cursor = automateManager.getAuditCache().query(sql)) {
				for (List<?> row : cursor) {
					int index = 0;
					HandlerExecuteInfo einfo = new HandlerExecuteInfo();
					einfo.setSre(rowGet(row, index++));
					einfo.setMetrics(rowGet(row, index++));
					einfo.setStatus(rowGet(row, index++));
					einfo.setMessage(rowGet(row, index++));
					einfo.setStdout(rowGet(row, index++));
					einfo.setStderr(rowGet(row, index++));

					Map<String, String> sre = einfo.getSre();
					einfo.setHost(MapUtils.getString(sre, AutomateManager.EXEC_HOST));
					einfo.setIp(MapUtils.getString(sre, AutomateManager.EXEC_IP));
					executeInfos.add(einfo);
				}
				pinfo.setExecuteInfos(executeInfos);
			}
		}
		return alarmTrace;
	}

	private Configuration getConfiguration(String xml) throws Exception {
		InputStream is = new ReaderInputStream(new StringReader(xml), UTF_8);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(is);
		Configuration config = new Configuration();
		config.configure(doc);
		return config;
	}

	private void createStatement(EPAdministrator admin, List<Alarmer> alarmers) throws Exception {
		int index = 0;
		for (Alarmer alarmer : alarmers) {
			index++;
			List<String> parseEpls = alarmer.parseEpls(null);
			for (String epl : parseEpls) {
				admin.createEPL(epl, alarmer.getName() + index, alarmer);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T rowGet(List<?> row, int index) {
		return (T) row.get(index);
	}
}
