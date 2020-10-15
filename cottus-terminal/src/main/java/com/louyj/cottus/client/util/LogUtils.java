package com.louyj.cottus.client.util;

import java.io.PrintStream;

import org.apache.commons.lang3.StringUtils;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class LogUtils {

	public static void log(String log, PrintStream writer) {
		writer.println("[INFO] " + log);
	}

	public static void clientError(String log, PrintStream writer) {
		writer.println("[CLIENT ERROR] " + log);
	}

	public static void serverError(String log, PrintStream writer) {
		writer.println("[SERVER ERROR] " + log);
	}

	public static void networkError(String log, PrintStream writer) {
		writer.println("[NETWORK ERROR] " + log);
	}

	public static void serverReject(BaseMessage message, PrintStream writer) {
		RejectMessage rejectMessage = (RejectMessage) message;
		writer.println("[SERVER ERROR] request rejected by server, reason " + rejectMessage.getReason());
	}

	public static void printMessage(String echo, PrintStream writer) {
		if (StringUtils.isBlank(echo)) {
			return;
		}
		if (StringUtils.isNotBlank(echo)) {
			if (echo.endsWith("\n")) {
				writer.print(echo);
			} else {
				writer.println(echo);
			}
		}

	}
}
