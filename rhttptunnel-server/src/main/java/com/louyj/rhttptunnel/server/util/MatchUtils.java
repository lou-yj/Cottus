package com.louyj.rhttptunnel.server.util;

import static com.louyj.rhttptunnel.server.util.PlaceHolderUtils.replacePlaceHolder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.jayway.jsonpath.DocumentContext;

/**
 *
 * Create at 2020年7月16日
 *
 * @author Louyj
 *
 */
public class MatchUtils {

	public static boolean isWindowMatched(boolean regexMatch, List<Map<String, Object>> sourceMapWin,
			Map<String, Object> matchedCond, DocumentContext envDc) {
		Map<String, Object> windowMatchedReplaced = Maps.newHashMap();
		for (Entry<String, Object> entry : matchedCond.entrySet()) {
			String matchedKey = replacePlaceHolder(envDc, entry.getKey());
			Object matchedValue = replacePlaceHolder(envDc, entry.getValue());
			windowMatchedReplaced.put(matchedKey, matchedValue);
		}
		for (Map<String, Object> windowMap : sourceMapWin) {
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
				return true;
			}
		}
		return false;
	}

	public static boolean isMatched(boolean regexMatch, Map<String, Object> sourceMap, Map<String, Object> matchedCond,
			DocumentContext envDc) {
		for (Entry<String, Object> entry : matchedCond.entrySet()) {
			String key = replacePlaceHolder(envDc, entry.getKey());
			Object expect = replacePlaceHolder(envDc, entry.getValue());
			Object value = sourceMap.get(key);
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

	static boolean isMatched(boolean regexMatch, Object value, Object expect) {
		if (value == null) {
			return false;
		}
		if (regexMatch) {
			return String.valueOf(value).matches(String.valueOf(expect));
		} else {
			return String.valueOf(value).equals(String.valueOf(expect));
		}
	}

	static boolean isMatched(boolean regexMatch, Object value, List<?> expects) {
		if (value == null) {
			return false;
		}
		for (Object expect : expects) {
			if (isMatched(regexMatch, value, expect)) {
				return true;
			}
		}
		return false;
	}

}
