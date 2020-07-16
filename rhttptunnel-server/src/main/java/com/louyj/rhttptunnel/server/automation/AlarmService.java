package com.louyj.rhttptunnel.server.automation;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.rhttptunnel.model.util.PlaceHolderUtils.replacePlaceHolder;

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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
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
import com.google.common.collect.Maps;
import com.jayway.jsonpath.DocumentContext;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.bean.automate.AlarmMarker;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.model.util.PlaceHolderUtils;
import com.louyj.rhttptunnel.server.automation.event.AlarmEvent;

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
			boolean matched = isMatched(alarmMarker.isRegexMatch(), eventMap, alarmMarker.getMatched());
			if (matched) {
				logger.info("Marker {} condition matched ", alarmMarker.getName());
				if (MapUtils.isNotEmpty(alarmMarker.getProperties())) {
					DocumentContext dc = PlaceHolderUtils.toDc(alarmMarker.getProperties());
					Map<String, Object> replacedTags = Maps.newHashMap();
					for (Entry<String, Object> entry : alarmMarker.getTags().entrySet()) {
						String key = replacePlaceHolder(dc, entry.getKey());
						Object value = replacePlaceHolder(dc, entry.getValue());
						replacedTags.put(key, value);
					}
					alarmEvent.getTags().add(Pair.of(alarmMarker.getName(), replacedTags));
				} else {
					alarmEvent.getTags().add(Pair.of(alarmMarker.getName(), Maps.newHashMap()));
				}
			}
		}
		logger.info("[{}] End mark alarm event", uuid);
		handlerService.handleAlarm(alarmEvent);
	}

	private boolean isMatched(boolean regexMatch, Map<String, Object> eventMap, Map<String, Object> matched) {
		for (Entry<String, Object> entry : matched.entrySet()) {
			String key = entry.getKey();
			Object expect = entry.getValue();
			Object value = eventMap.get(key);
			if (expect instanceof List) {
				if (isMatched(regexMatch, value, (List<?>) expect) == false) {
					return false;
				}
			} else {
				if (isMatched(regexMatch, value, expect) == false) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isMatched(boolean regexMatch, Object value, Object expect) {
		if (value == null) {
			return false;
		}
		if (regexMatch) {
			return String.valueOf(value).matches(String.valueOf(expect));
		} else {
			return String.valueOf(value).equals(String.valueOf(expect));
		}
	}

	private boolean isMatched(boolean regexMatch, Object value, List<?> expects) {
		for (Object expect : expects) {
			if (isMatched(regexMatch, value, expect)) {
				return true;
			}
		}
		return false;
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

}
