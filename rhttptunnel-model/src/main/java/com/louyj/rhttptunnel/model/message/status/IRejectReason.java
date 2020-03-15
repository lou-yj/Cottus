package com.louyj.rhttptunnel.model.message.status;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public interface IRejectReason {

	String reason();

	public static IRejectReason make(String reason) {
		IRejectReason result = new IRejectReason() {

			@Override
			public String reason() {
				return reason;
			}

		};
		return result;
	}

}
