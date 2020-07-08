package com.louyj.rhttptunnel.server.automation;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
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

	public AtomicReference<EPRuntime> getEpRuntime() {
		return epRuntime;
	}

	public AtomicReference<EPRuntime> resetEsper(String config, String epls) {
		try {
			String oldProviderURI = String.format("provider-%s-%d", uuid, providerCounter);
			String currentProviderURI = String.format("provider-%s-%d", uuid, ++providerCounter);
			try {
				epService = EPServiceProviderManager.getProvider(currentProviderURI, getConfiguration(config));
				epService.initialize();
				epService.addStatementStateListener(this);
				EPAdministrator admin = epService.getEPAdministrator();
				createStatement(admin, epls);
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

	private Configuration getConfiguration(String xml) throws Exception {
		InputStream is = new ReaderInputStream(new StringReader(xml), UTF_8);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(is);
		Configuration config = new Configuration();
		config.configure(doc);
		return config;
	}

	private void createStatement(EPAdministrator admin, String epls) throws Exception {
		epls = Pattern.compile("/\\*.*?\\*/", CASE_INSENSITIVE | DOTALL).matcher(epls).replaceAll("");
		epls = epls.replaceAll("//.*", "");
		String[] sqls = epls.split(";");
		for (String sql : sqls) {
			sql = StringUtils.trim(sql);
			if (StringUtils.isBlank(sql)) {
				continue;
			}
			admin.createEPL(sql);
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
				if (name.equalsIgnoreCase("listener")) {
					String ruleName = tagAnno.value();
					AlarmEventListener listener = new AlarmEventListener(ruleName);
					statement.addListener(listener);
					logger.info("Add Listener for rule: {}.", ruleName);
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
		logger.info("Esper service destoryed.");
	}

}
