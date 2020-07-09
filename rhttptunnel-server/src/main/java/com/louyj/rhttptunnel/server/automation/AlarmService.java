package com.louyj.rhttptunnel.server.automation;

import static com.google.common.base.Charsets.UTF_8;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;

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

	public AlarmService(HandlerService handlerService) {
		super();
		this.handlerService = handlerService;
		try {
			URL resource = ClassLoader.getSystemClassLoader().getResource("esper.xml");
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
					AlarmEventListener listener = new AlarmEventListener(alarmer, handlerService);
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
