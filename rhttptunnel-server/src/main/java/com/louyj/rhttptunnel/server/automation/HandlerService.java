package com.louyj.rhttptunnel.server.automation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.MapUtils;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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
public class HandlerService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Pattern pattern = Pattern.compile("\\{\\{(?<ph>.*?)\\}\\}", Pattern.MULTILINE | Pattern.DOTALL);

	private ObjectMapper jackson = JsonUtils.jackson();
	private List<Handler> handlers;
	private IgniteCache<Object, Object> alarmCache;

	public HandlerService(List<Handler> handlers, IgniteCache<Object, Object> alarmCache) {
		super();
		this.handlers = handlers;
		this.alarmCache = alarmCache;
	}

	public List<Handler> getHandlers() {
		return handlers;
	}

	public void setHandlers(List<Handler> handlers) {
		this.handlers = handlers;
	}

	public void handleAlarm(AlarmEvent alarmEvent) {
		try {
			String uuid = UUID.randomUUID().toString();
			logger.info("[{}] Receive alarm event {}", uuid, jackson.writeValueAsString(alarmEvent));
			alarmEvent.setUuid(uuid);
			alarmCache.put(uuid, alarmEvent);
			String alarmGroup = alarmEvent.getAlarmGroup();
			logger.info("[{}] Alarm group {}", alarmGroup);
			Map<String, Object> eventMap = alarmEvent.toMap();
			DocumentContext eventDc = JsonPath.parse(jackson.writeValueAsString(eventMap));
			for (Handler handler : handlers) {
				logger.info("[{}] Start eval handler {}", uuid, handler.getUuid());
				boolean isMatched = matched(uuid, eventMap, eventDc, handler.isRegexMatch(), handler.getMatched(),
						handler.getWindowMatched(), handler.getTimeWindowSize());
				if (isMatched == false) {
					continue;
				}
				logger.info("[{}] All condition matched", uuid);

				String infoUUid = UUID.randomUUID().toString();
				AlarmHandlerInfo alarmHandlerInfo = new AlarmHandlerInfo();
				alarmHandlerInfo.setUuid(infoUUid);
				alarmHandlerInfo.setHandlerId(handler.getUuid());
				alarmHandlerInfo.setAlarmId(uuid);
				alarmCache.put(infoUUid, alarmHandlerInfo);
				if (handler.getActionWaitCount() > 0) {
					long timeDeadLine = System.currentTimeMillis() - handler.getTimeWindowSize() * 1000;
					SqlFieldsQuery sql = new SqlFieldsQuery(
							"SELECT count(1) FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId and handlerId = ? and alarmGroup=? and alarmTime > ?")
									.setArgs(handler.getUuid(), alarmGroup, timeDeadLine);
					try (QueryCursor<List<?>> cursor = alarmCache.query(sql)) {
						List<?> firstRow = cursor.iterator().next();
						long count = (long) firstRow.get(0);
						if (count < handler.getActionWaitCount()) {
							logger.info("[{}] Current wait count {} little than handler actionWaitCount {}", uuid,
									count, handler.getActionWaitCount());
							continue;
						}
					}
				} else if (handler.getActionAggrTime() > 0) {
					long timeDeadLine = System.currentTimeMillis() - handler.getActionAggrTime() * 1000;
					SqlFieldsQuery sql = new SqlFieldsQuery(
							"SELECT count(1) FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId and handled=true and handlerId = ? and alarmGroup=? and alarmTime > ?")
									.setArgs(handler.getUuid(), alarmGroup, timeDeadLine);
					try (QueryCursor<List<?>> cursor = alarmCache.query(sql)) {
						List<?> firstRow = cursor.iterator().next();
						long count = (long) firstRow.get(0);
						if (count > 0) {
							logger.info("[{}] Current group already has alarm handled in last {} seconds", uuid,
									handler.getActionAggrTime());
							continue;
						}
					}
				}
				doHandle(handler, alarmEvent, alarmHandlerInfo);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void doHandle(Handler handler, AlarmEvent alarmEvent, AlarmHandlerInfo alarmHandlerInfo) {

		alarmHandlerInfo.setHandled(true);
		alarmCache.put(alarmHandlerInfo.getUuid(), alarmHandlerInfo);
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
		if (MapUtils.isNotEmpty(windowMatched)) {
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

}
