package com.louyj.rhttptunnel.server.automation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.louyj.rhttptunnel.model.bean.automate.Handler;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.automation.event.AlarmEvent;

/**
 *
 * Create at 2020年7月9日
 *
 * @author Louyj
 *
 */
public class HandlerService extends TimerTask {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Pattern pattern = Pattern.compile("\\{\\{(?<ph>.*?)\\}\\}", Pattern.MULTILINE | Pattern.DOTALL);

	private ObjectMapper jackson = JsonUtils.jackson();
	private IgniteCache<Object, Object> alarmCache;
	private AutomateManager automateManager;
	private Timer timer;

	public HandlerService(AutomateManager automateManager, IgniteCache<Object, Object> alarmCache) {
		super();
		this.automateManager = automateManager;
		this.alarmCache = alarmCache;
		this.timer = new Timer(true);
		this.timer.schedule(this, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
	}

	public void handleAlarm(AlarmEvent alarmEvent) {
		try {
			Map<String, Object> eventMap = alarmEvent.toMap();
			String uuid = UUID.randomUUID().toString();
			logger.info("[{}] Receive alarm event {}", uuid, jackson.writeValueAsString(eventMap));
			alarmEvent.setUuid(uuid);
			alarmCache.put(uuid, alarmEvent);
			String alarmGroup = alarmEvent.getAlarmGroup();
			logger.info("[{}] Alarm group {}", uuid, alarmGroup);
			DocumentContext eventDc = JsonPath.parse(jackson.writeValueAsString(eventMap));
			Set<String> preventHandlers = Sets.newHashSet();
			for (Handler handler : automateManager.getHandlers()) {
				logger.info("[{}] Start eval handler {}", uuid, handler.getName());
				boolean isMatched = matched(uuid, eventMap, eventDc, handler.isRegexMatch(), handler.getMatched(),
						handler.getWindowMatched(), handler.getTimeWindowSize());
				if (isMatched == false) {
					continue;
				}
				logger.info("[{}] All condition matched", uuid);
				String infoUUid = UUID.randomUUID().toString();
				AlarmHandlerInfo alarmHandlerInfo = new AlarmHandlerInfo();
				alarmHandlerInfo.setUuid(infoUUid);
				alarmHandlerInfo.setHandlerId(handler.getName());
				alarmHandlerInfo.setAlarmId(uuid);
				alarmHandlerInfo.setActionWaitCount(handler.getActionWaitCount());
				alarmHandlerInfo.setActionAggrTime(handler.getActionAggrTime());
				alarmCache.put(infoUUid, alarmHandlerInfo);
				List<AlarmEvent> correlationAlarms = Lists.newArrayList();
				if (handler.getActionWaitCount() > 0) {
					long timeDeadLine = System.currentTimeMillis() - handler.getTimeWindowSize() * 1000;
					SqlFieldsQuery sql = new SqlFieldsQuery(
							"SELECT info.uuid, alarm.uuid, alarmTime FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId and handlerId = ? and alarmGroup=? and alarmTime > ? order by alarmTime desc")
									.setArgs(handler.getName(), alarmGroup, timeDeadLine);
					Pair<List<AlarmEvent>, List<AlarmHandlerInfo>> pair = parseCorrelationAlarms(sql);
					List<AlarmEvent> alarmEvents = pair.getLeft();
					if (CollectionUtils.size(alarmEvents) < handler.getActionWaitCount()) {
						logger.info("[{}] Current wait count {} little than handler actionWaitCount {}", uuid,
								CollectionUtils.size(alarmEvents), handler.getActionWaitCount());
						continue;
					} else {
						correlationAlarms = alarmEvents;
					}
				} else if (handler.getActionAggrTime() > 0) {
					long timeDeadLine = System.currentTimeMillis() - handler.getActionAggrTime() * 1000;
					SqlFieldsQuery sql = new SqlFieldsQuery(
							"SELECT count(1) FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId and handled=true and handlerId = ? and alarmGroup=? and alarmTime > ?")
									.setArgs(handler.getName(), alarmGroup, timeDeadLine);
					try (QueryCursor<List<?>> cursor = alarmCache.query(sql)) {
						List<?> firstRow = cursor.iterator().next();
						Long count = (Long) firstRow.get(0);
						if (count > 0) {
							logger.info("[{}] Current group already has alarm handled in last {} seconds", uuid,
									handler.getActionAggrTime());
							continue;
						} else {
							long timeDeadline = System.currentTimeMillis() - handler.getActionAggrTime() * 1000 * 10;
							SqlFieldsQuery sql1 = new SqlFieldsQuery(
									"SELECT info.uuid, alarm.uuid, alarmTime FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId and handlerId=? and alarmGroup=? and alarmTime>=? and handled=false order by alarmTime desc")
											.setArgs(handler.getName(), alarmGroup, timeDeadline);
							Pair<List<AlarmEvent>, List<AlarmHandlerInfo>> pair = parseCorrelationAlarms(sql1);
							List<AlarmEvent> alarmEvents = pair.getKey();
							correlationAlarms = alarmEvents;
						}
					}
				}
				Pair<Boolean, String> preventPair = isPrevent(preventHandlers, handler.getName());
				if (preventPair.getLeft() == false) {
					doHandle(handler, alarmEvent, correlationAlarms, alarmHandlerInfo, eventDc);
					if (CollectionUtils.isNotEmpty(handler.getPreventHandlers())) {
						preventHandlers.addAll(handler.getPreventHandlers());
					}
				} else {
					logger.info("[{}] handler {} is prevent by expression {}", uuid, handler.getName(),
							preventPair.getRight());
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private Pair<Boolean, String> isPrevent(Set<String> preventHandlers, String handler) {
		for (String preventHandler : preventHandlers) {
			if (handler.matches(preventHandler)) {
				return Pair.of(true, preventHandler);
			}
		}
		return Pair.of(false, "");
	}

	private void doHandle(Handler handler, AlarmEvent alarmEvent, List<AlarmEvent> correlationAlarms,
			AlarmHandlerInfo alarmHandlerInfo, DocumentContext eventDc) {
		Map<String, String> targetMap = Maps.newHashMap();
		for (Entry<String, String> entry : handler.getTargets().entrySet()) {
			targetMap.put(replacePlaceHolder(eventDc, entry.getKey()), replacePlaceHolder(eventDc, entry.getValue()));
		}
		automateManager.scheduleHandler(handler, alarmEvent, correlationAlarms, alarmHandlerInfo, targetMap);
	}

	private boolean matched(String uuid, Map<String, Object> eventMap, DocumentContext evnetDc, boolean regexMatch,
			Map<String, Object> matched, Map<String, Object> windowMatched, int timeWindowSize) {
		for (Entry<String, Object> entry : matched.entrySet()) {
			String matchedKey = replacePlaceHolder(evnetDc, entry.getKey());
			Object matchedValue = replacePlaceHolder(evnetDc, entry.getValue());
			Object eventValue = eventMap.get(matchedKey);
			if (isMatched(regexMatch, matchedValue, eventValue) == false) {
				return false;
			}
		}
		logger.info("[{}] Condition matched", uuid);
		if (isWindowMatched(uuid, evnetDc, regexMatch, windowMatched, timeWindowSize)) {
			logger.info("[{}] Window condition matched", uuid);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean isWindowMatched(String uuid, DocumentContext evnetDc, boolean regexMatch,
			Map<String, Object> windowMatched, int timeWindowSize) {
		if (MapUtils.isEmpty(windowMatched)) {
			return true;
		}
		Map<String, Object> windowMatchedReplaced = Maps.newHashMap();
		for (Entry<String, Object> entry : windowMatched.entrySet()) {
			String matchedKey = replacePlaceHolder(evnetDc, entry.getKey());
			Object matchedValue = replacePlaceHolder(evnetDc, entry.getValue());
			windowMatchedReplaced.put(matchedKey, matchedValue);
		}
		long timeDeadLine = System.currentTimeMillis() - timeWindowSize * 1000;
		SqlFieldsQuery sql = new SqlFieldsQuery("SELECT uuid,fields FROM AlarmEvent where alarmTime > ?")
				.setArgs(timeDeadLine);
		try (QueryCursor<List<?>> cursor = alarmCache.query(sql)) {
			for (List<?> row : cursor) {
				Map<String, Object> windowMap = (Map<String, Object>) row.get(1);
				boolean allMatched = true;
				for (Entry<String, Object> entry : windowMatchedReplaced.entrySet()) {
					String matchedKey = entry.getKey();
					Object matchedValue = entry.getValue();
					Object windowValue = windowMap.get(matchedKey);
					if (isMatched(regexMatch, matchedValue, windowValue) == false) {
						allMatched = false;
						break;
					}
				}
				if (allMatched) {
					logger.info("[{}] Matched window event {}", windowMap);
					return true;
				}
			}
		}
		return false;
	}

	private boolean isMatched(boolean regexMatch, Object matchedValue, Object eventValue) {
		if (matchedValue == null || eventValue == null) {
			return false;
		}
		if (regexMatch) {
			return String.valueOf(eventValue).matches(String.valueOf(matchedValue));
		} else {
			return matchedValue.equals(eventValue);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T replacePlaceHolder(DocumentContext evnetDc, T content) {
		if (!(content instanceof String)) {
			return content;
		}
		String contentStr = (String) content;
		Matcher matcher = pattern.matcher(contentStr);
		while (matcher.find()) {
			String ph = matcher.group("ph");
			Object value = tryGet(evnetDc, ph);
			if (value == null) {
				value = "";
			}
			contentStr = matcher.replaceAll(Matcher.quoteReplacement(String.valueOf(value)));
			matcher = pattern.matcher(contentStr);
		}
		return (T) contentStr;
	}

	private Object tryGet(DocumentContext dc, String key) {
		try {
			return dc.read(key);
		} catch (Exception e) {
			logger.warn("find key {} exception", key, e);
			return null;
		}
	}

	private Pair<List<AlarmEvent>, List<AlarmHandlerInfo>> parseCorrelationAlarms(SqlFieldsQuery sql1) {
		List<AlarmEvent> alarmEvents = Lists.newArrayList();
		List<AlarmHandlerInfo> alarmHanders = Lists.newArrayList();
		try (QueryCursor<List<?>> cursor1 = alarmCache.query(sql1)) {
			for (List<?> row1 : cursor1) {
				String infoUuid = (String) row1.get(0);
				String alarmUuid = (String) row1.get(1);
				alarmEvents.add((AlarmEvent) alarmCache.get(alarmUuid));
				alarmHanders.add((AlarmHandlerInfo) alarmCache.get(infoUuid));
			}
		}
		return Pair.of(alarmEvents, alarmHanders);
	}

	@Override
	public void run() {
		try {
			SqlFieldsQuery sql = new SqlFieldsQuery(
					"SELECT handlerId, alarmGroup, max(alarmTime) filter(where handled=true), count(1) filter(where handled=false) FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId group by handlerId,alarmGroup");
			try (QueryCursor<List<?>> cursor = alarmCache.query(sql)) {
				for (List<?> row : cursor) {
					String handlerId = (String) row.get(0);
					String alarmGroup = (String) row.get(1);
					Long maxAlarmTime = (Long) row.get(2);
					Long count = (Long) row.get(3);
					if (count <= 0) {
						continue;
					}
					Handler handler = automateManager.getHandler(handlerId);
					if (handler == null) {
						logger.warn("Handler {} not found.", handlerId);
						continue;
					}
					if (handler.getActionWaitCount() > 0) {
						continue;
					}
					if (handler.getActionAggrTime() <= 0) {
						continue;
					}
					int actionAggrTime = handler.getActionAggrTime();
					if (System.currentTimeMillis() - actionAggrTime * 1000 > maxAlarmTime) {
						SqlFieldsQuery sql1 = new SqlFieldsQuery(
								"SELECT info.uuid, alarm.uuid, alarmTime FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId and handlerId=? and alarmGroup=? and alarmTime>=? and handled=false order by alarmTime desc")
										.setArgs(handlerId, alarmGroup, maxAlarmTime);
						Pair<List<AlarmEvent>, List<AlarmHandlerInfo>> pair = parseCorrelationAlarms(sql1);
						List<AlarmEvent> alarmEvents = pair.getLeft();
						List<AlarmHandlerInfo> alarmHanders = pair.getRight();
						if (CollectionUtils.isEmpty(alarmEvents)) {
							continue;
						}
						AlarmEvent alarmEvent = alarmEvents.get(0);
						AlarmHandlerInfo alarmHandlerInfo = alarmHanders.get(0);
						String json = jackson.writeValueAsString(alarmEvent);
						DocumentContext eventDc = JsonPath.parse(json);
						Map<String, String> targetMap = Maps.newHashMap();
						for (Entry<String, String> entry : handler.getTargets().entrySet()) {
							targetMap.put(replacePlaceHolder(eventDc, entry.getKey()),
									replacePlaceHolder(eventDc, entry.getValue()));
						}
						automateManager.scheduleHandler(handler, alarmEvent, alarmEvents, alarmHandlerInfo,
								handler.getTargets());
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
