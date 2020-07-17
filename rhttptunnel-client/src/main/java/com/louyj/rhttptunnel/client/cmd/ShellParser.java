package com.louyj.rhttptunnel.client.cmd;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * Create at 2020年7月16日
 *
 * @author Louyj
 *
 */
public class ShellParser {

	private StringBuffer buffer = new StringBuffer();

	public String parse(String line) {
		if (StringUtils.isBlank(line)) {
			return null;
		}
		if (line.trim().endsWith(";")) {
			return flushWithBuffer(line);
		}
		if (line.endsWith("\\")) {
			buffer.append(line).append("\n");
			return null;
		}
		return flushWithBuffer(line);
	}

	private String flushWithBuffer(String line) {
		if (buffer.length() > 0) {
			String result = buffer.toString() + line;
			buffer = new StringBuffer();
			return result;
		} else {
			return line;
		}
	}

}
