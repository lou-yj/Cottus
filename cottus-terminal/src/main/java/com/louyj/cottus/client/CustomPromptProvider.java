package com.louyj.cottus.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jline.utils.AttributedString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
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

	@Override
	public AttributedString getPrompt() {
		if (session.isServerConnected() == false) {
			return new AttributedString("DISCONNECT:> ");
		}
		if (session.isWorkerConnected() == false) {
			return new AttributedString("SERVER:> ");
		}
		List<ClientInfo> workers = session.getSelectedWorkers();
		if (workers.size() == 1) {
			ClientInfo worker = workers.get(0);
			return new AttributedString("WORKER[" + worker.getHost() + "(" + worker.getIp() + ")]:> ");
		} else {
			List<String> items = Lists.newArrayList();
			for (int i = 0; i < 3 && i < workers.size(); i++) {
				items.add(workers.get(i).getHost());
			}
			String text = StringUtils.join(items, ",");
			if (workers.size() > 3) {
				text = text + ",...";
			}
			return new AttributedString("WORKER[" + text + "]:> ");
		}
	}

}
