package com.louyj.rhttptunnel.client;

import org.jline.utils.AttributedString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.extend.AsyncShellResultHanlder;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class CustomPromptProvider implements PromptProvider {

	@Autowired
	private ClientSession session;
	@Autowired
	private AsyncShellResultHanlder asyncShellResultHanlder;

	@Override
	public AttributedString getPrompt() {
//		System.out.println(asyncShellResultHanlder.isWaiting());
//		if (asyncShellResultHanlder.isWaiting()) {
//			return new AttributedString("");
//		}
		if (session.isServerConnected() == false) {
			return new AttributedString("DISCONNECT:> ");
		}
		if (session.isWorkerConnected() == false) {
			return new AttributedString("SERVER:> ");
		}
		ClientInfo worker = session.getSelectedWorker();
		return new AttributedString("WORKER[" + worker.getHost() + "(" + worker.getIp() + ")]:> ");
	}

}
