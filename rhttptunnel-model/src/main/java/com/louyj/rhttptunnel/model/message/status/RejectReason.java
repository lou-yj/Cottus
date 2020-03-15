package com.louyj.rhttptunnel.model.message.status;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public enum RejectReason implements IRejectReason {

	INTERNEL_SERVER_ERROR("INTERNEL SERVER ERROR"),

	CLIENT_ERROR("CLIENT ERROR"),

	BAD_REQUEST("BAD REQUEST"),

	ACCESS_DENY("ACCESS DENY"),

	NOT_SUPPORT_OPERATION("NOT SUPPORT OPERATION"),

	INTERRUPT("INTERRUPT"),

	ACCESS_FILE_FAILED("ACCESS FILE FAILED"),

	SERVER_BAD_RESPONSE("SERVER BAD RESPONSE"),

	;

	private String reason;

	private RejectReason(String status) {
		this.reason = status;
	}

	@Override
	public String reason() {
		return reason;
	}
}
