package com.louyj.rhttptunnel.model.message;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import com.louyj.rhttptunnel.model.message.status.IRejectReason;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class RejectMessage extends BaseMessage {

	private IRejectReason reason;

	public RejectMessage(ClientInfo client, IRejectReason reason) {
		super(client);
		this.reason = reason;
	}

	public IRejectReason getReason() {
		return reason;
	}

	public static RejectMessage creason(ClientInfo client, String exchangeId, IRejectReason reason) {
		return new RejectMessage(client, reason);
	}

	public static RejectMessage sreason(String exchangeId, IRejectReason reason) {
		return new RejectMessage(SERVER, reason);
	}

}
