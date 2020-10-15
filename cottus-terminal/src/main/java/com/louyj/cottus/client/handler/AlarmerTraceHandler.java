package com.louyj.cottus.client.handler;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.louyj.cottus.client.ClientSession;
import com.louyj.cottus.client.exception.EndOfMessageException;
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

import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.bean.automate.AlarmInhibitor;
import com.louyj.rhttptunnel.model.bean.automate.AlarmSilencer;
import com.louyj.rhttptunnel.model.bean.automate.AlarmTrace;
import com.louyj.rhttptunnel.model.bean.automate.AlarmTriggeredRecord;
import com.louyj.rhttptunnel.model.bean.automate.HandlerProcessInfo;
import com.louyj.rhttptunnel.model.bean.automate.HandlerProcessInfo.HandlerExecuteInfo;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.automate.AlarmerTraceMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AlarmerTraceHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Autowired
	@Lazy
	private Terminal terminal;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmerTraceMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		AlarmerTraceMessage traceMessage = (AlarmerTraceMessage) message;
		AlarmTrace alarmTrace = traceMessage.getAlarmTrace();
		AlarmTriggeredRecord record = alarmTrace.getRecord();

		DefaultNode root = new DefaultNode("Alarm Trace For ID " + record.getUuid());
		{
			DefaultNode recordNode = new DefaultNode("Alarm Event");
			addChildNode(recordNode, "ID", record.getUuid());
			addChildNode(recordNode, "Alarm Time", formatTime(record.getAlarmTime()));
			addChildNode(recordNode, "Alarm Group", record.getAlarmGroup());
			addChildNode(recordNode, "Alarm Event Fields", record.getFields());
			root.addChild(recordNode);
		}
		{
			List<Pair<String, Map<String, Object>>> tags = record.getTags();
			if (CollectionUtils.isNotEmpty(tags)) {
				DefaultNode markersNode = new DefaultNode("Alarm Markers");
				for (Pair<String, Map<String, Object>> pair : tags) {
					addChildNode(markersNode, "Tag Added By Marker " + pair.getLeft(), pair.getRight());
				}
				root.addChild(markersNode);
			}
		}
		{
			AlarmSilencer alarmSilencer = alarmTrace.getAlarmSilencer();
			if (alarmSilencer != null) {
				DefaultNode silenceNode = new DefaultNode("Alarm Silencer");
				addChildNode(silenceNode, "Match Condition", alarmSilencer.getMatched());
				addChildNode(silenceNode, "Regex", String.valueOf(alarmSilencer.isRegexMatch()));
				addChildNode(silenceNode, "Start Time", formatTime(alarmSilencer.getStartTime()));
				addChildNode(silenceNode, "End Time", formatTime(alarmSilencer.getEndTime()));
				root.addChild(silenceNode);
			}
		}
		{
			AlarmInhibitor alarmInhibitor = alarmTrace.getAlarmInhibitor();
			if (alarmInhibitor != null) {
				DefaultNode inhibitorNode = new DefaultNode("Alarm Inhibitor");
				addChildNode(inhibitorNode, "Name", alarmInhibitor.getName());
				addChildNode(inhibitorNode, "Regex", String.valueOf(alarmInhibitor.isRegexMatch()));
				addChildNode(inhibitorNode, "Match Condition", alarmInhibitor.getMatched());
				addChildNode(inhibitorNode, "Window Match Condition", alarmInhibitor.getWindowMatched());
				addChildNode(inhibitorNode, "Time Window Size", String.valueOf(alarmInhibitor.getTimeWindowSize()));
				root.addChild(inhibitorNode);
			}
		}
		{
			DefaultNode handlersNode = new DefaultNode("Alarm Handlers");
			for (HandlerProcessInfo pinfo : alarmTrace.getHandlerInfos()) {
				DefaultNode handlerNode = new DefaultNode("Handler " + pinfo.getHandlerId());
				addChildNode(handlerNode, "Evaluate Time", formatTime(pinfo.getEvaluateTime()));
				addChildNode(handlerNode, "Status", pinfo.getStatus() == null ? "" : pinfo.getStatus().name());
				addChildNode(handlerNode, "Message", defaultString(pinfo.getMessage()));
				if (pinfo.getPreventedBy() != null) {
					addChildNode(handlerNode, "Prevented By", String.format("%s[%s]", pinfo.getPreventedBy().getLeft(),
							pinfo.getPreventedBy().getRight()));
				}
				if (pinfo.getScheduledTime() != null) {
					addChildNode(handlerNode, "Scheduled Time", formatTime(pinfo.getScheduledTime()));
				}
				if (CollectionUtils.isNotEmpty(pinfo.getTargetHosts())) {
					DefaultNode targetHostsNode = new DefaultNode("Target Hosts");
					for (ClientInfo th : pinfo.getTargetHosts()) {
						addChildNode(targetHostsNode, String.format("%s[%s]", th.getHost(), th.getIp()));
					}
					handlerNode.addChild(targetHostsNode);
				}
				if (CollectionUtils.isNotEmpty(pinfo.getCorrelationAlarms())) {
					DefaultNode corrsNode = new DefaultNode("Correlation Alarm Events");
					int index = 1;
					for (Map<String, Object> cae : pinfo.getCorrelationAlarms()) {
						addChildNode(corrsNode, "Correlation Alarm Event " + index++, cae);
					}
					handlerNode.addChild(corrsNode);
				}
				if (MapUtils.isNotEmpty(pinfo.getParams())) {
					addChildNode(handlerNode, "Handler Execution Parameters", pinfo.getParams());
				}
				if (CollectionUtils.isNotEmpty(pinfo.getExecuteInfos())) {
					DefaultNode hesNode = new DefaultNode("Handler Execution Details");
					for (HandlerExecuteInfo heInfo : pinfo.getExecuteInfos()) {
						DefaultNode heNode = new DefaultNode(String.format("Worker %s", heInfo.getHost()));
						addChildNode(heNode, "Host", heInfo.getHost());
						addChildNode(heNode, "Ip", heInfo.getIp());
						addChildNode(heNode, "Status", heInfo.getStatus() == null ? "" : heInfo.getStatus().name());
						addChildNode(heNode, "Message", heInfo.getMessage());
						if (MapUtils.isNotEmpty(heInfo.getSre())) {
							addChildNode(heNode, "Script Runtime Environment", heInfo.getSre());
						}
						if (CollectionUtils.isNotEmpty(heInfo.getMetrics())) {
							DefaultNode metricsNode = new DefaultNode("Metrics");
							for (String metrics : heInfo.getMetrics()) {
								addChildNode(metricsNode, metrics);
							}
							heNode.addChild(metricsNode);
						}
						addChildNode(heNode, "Standard Output", defaultString(heInfo.getStdout()));
						addChildNode(heNode, "Standard Error", defaultString(heInfo.getStderr()));
						hesNode.addChild(heNode);
					}
					handlerNode.addChild(hesNode);
				}
				handlersNode.addChild(handlerNode);
			}
			root.addChild(handlersNode);
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
