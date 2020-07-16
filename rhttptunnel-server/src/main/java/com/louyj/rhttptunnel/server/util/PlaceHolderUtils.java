package com.louyj.rhttptunnel.server.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Create at 2020年7月15日
 *
 * @author Louyj
 *
 */
public class PlaceHolderUtils {

	private static Logger logger = LoggerFactory.getLogger(PlaceHolderUtils.class);

	private static Pattern pattern = Pattern.compile("\\{\\{(?<ph>.*?)\\}\\}", Pattern.MULTILINE | Pattern.DOTALL);

	private static ObjectMapper jackson = JsonUtils.jackson();

	@SuppressWarnings("unchecked")
	public static <T> T replacePlaceHolder(DocumentContext evnetDc, T content) {
		if (evnetDc == null) {
			return content;
		}
		if (!(content instanceof String)) {
			return content;
		}
		String contentStr = (String) content;
		Matcher matcher = pattern.matcher(contentStr);
		while (matcher.find()) {
			String ph = matcher.group("ph");
			if (ph.startsWith("$.") == false) {
				ph = "$." + ph;
			}
			Object value = tryGet(evnetDc, ph);
			if (value == null) {
				value = "";
			}
			contentStr = matcher.replaceAll(Matcher.quoteReplacement(String.valueOf(value)));
			matcher = pattern.matcher(contentStr);
		}
		return (T) contentStr;
	}

	public static <T> T replacePlaceHolder(Map<String, ?> map, T content) throws JsonProcessingException {
		return replacePlaceHolder(toDc(map), content);
	}

	public static DocumentContext toDc(Map<String, ?> map) {
		try {
			String json = jackson.writeValueAsString(map);
			return JsonPath.parse(json);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Object tryGet(DocumentContext dc, String key) {
		try {
			return dc.read(key);
		} catch (Exception e) {
			logger.warn("find key {} exception", key, e);
			return null;
		}
	}

}
