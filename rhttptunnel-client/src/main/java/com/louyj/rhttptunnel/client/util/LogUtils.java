package com.louyj.rhttptunnel.client.util;

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

	public static void log(String log) {
		System.out.println("[INFO] " + log);
	}

	public static void clientError(String log) {
		System.out.println("[CLIENT ERROR] " + log);
	}

	public static void serverError(String log) {
		System.out.println("[SERVER ERROR] " + log);
	}

	public static void networkError(String log) {
		System.out.println("[NETWORK ERROR] " + log);
	}

	public static void serverReject(BaseMessage message) {
		RejectMessage rejectMessage = (RejectMessage) message;
		System.out.println("[SERVER ERROR] request rejected by server, reason " + rejectMessage.getReason());
	}

	public static void printMessage(String echo) {
		if (echo == null) {
			return;
		}
		if (StringUtils.isNotBlank(echo)) {
			if (echo.endsWith("\n")) {
				System.out.print(echo);
			} else {
				System.out.println(echo);
			}
		}
	}
}
