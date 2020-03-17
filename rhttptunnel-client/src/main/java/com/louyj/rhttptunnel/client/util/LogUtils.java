package com.louyj.rhttptunnel.client.util;

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
}
