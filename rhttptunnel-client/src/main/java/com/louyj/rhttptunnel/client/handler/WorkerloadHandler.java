package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyle;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.bean.worker.Workerload;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.WorkerLoadMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class WorkerloadHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return WorkerLoadMessage.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		WorkerLoadMessage loadMessage = (WorkerLoadMessage) message;
		Workerload workerLoad = loadMessage.getWorkload();

		List<String> clientIds = workerLoad.getClientIds();

		DefaultNode root = new DefaultNode("Worker Information");
		{
			addChildNode(root, "Connected Clients", clientIds);
		}
		{
			Map<String, Object> systemInfo = JsonUtils.jackson().convertValue(workerLoad.getSystemLoadInfo(),
					Map.class);
			addChildNode(root, "System Load Info", systemInfo);
		}
		{
			Map<String, Object> vmInfo = JsonUtils.jackson().convertValue(workerLoad.getVmLoadInfo(), Map.class);
			addChildNode(root, "VM Load Info", vmInfo);
		}
		TreeOptions options = new TreeOptions();
		options.setStyle(new TreeStyle("│   ", "├───", "╰───", "(", ")", "<", ">"));
		String rendered = TextTree.newInstance(options).render(root);
		writer.println(rendered);
		throw new EndOfMessageException();

	}

	@SuppressWarnings("unchecked")
	private void addChildNode(DefaultNode parent, String name, Map<String, ?> map) {
		DefaultNode mapNode = new DefaultNode(name);
		for (Entry<String, ?> entry : map.entrySet()) {
			Object value = entry.getValue();
			String key = entry.getKey();
			if (value instanceof Map) {
				Map<String, ?> mapValue = (Map<String, ?>) value;
				if (MapUtils.isNotEmpty(mapValue))
					addChildNode(mapNode, key, mapValue);
			} else if (value instanceof List) {
				List<?> listValue = (List<?>) value;
				if (CollectionUtils.isNotEmpty(listValue))
					addChildNode(mapNode, key, listValue);
			} else {
				addChildNode(mapNode, key, String.valueOf(value));
			}
		}
		parent.addChild(mapNode);
	}

	private void addChildNode(DefaultNode parent, String key, List<?> list) {
		DefaultNode listNode = new DefaultNode(key);
		for (Object item : list) {
			addChildNode(listNode, String.valueOf(item));
		}
		parent.addChild(listNode);
	}

	private void addChildNode(DefaultNode parent, String key, String value) {
		parent.addChild(new DefaultNode(String.format("%s: %s", key, value)));
	}

	private void addChildNode(DefaultNode parent, String key) {
		parent.addChild(new DefaultNode(key));
	}

}
