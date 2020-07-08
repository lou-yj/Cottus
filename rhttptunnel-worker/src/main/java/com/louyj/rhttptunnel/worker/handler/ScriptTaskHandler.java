package com.louyj.rhttptunnel.worker.handler;

import static com.google.common.base.Charsets.UTF_8;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.python.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.server.TaskLogMessage;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsCollectType;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsType;
import com.louyj.rhttptunnel.worker.ClientDetector;
import com.louyj.rhttptunnel.worker.script.EvalResult;
import com.louyj.rhttptunnel.worker.script.ScriptEngineExecutor;
import com.louyj.rhttptunnel.worker.script.metrics.FileMetricsCollector;
import com.louyj.rhttptunnel.worker.script.metrics.IMetricsCollector;
import com.louyj.rhttptunnel.worker.script.metrics.IMetricsParser;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ScriptTaskHandler implements IMessageHandler, ApplicationContextAware {

	@Autowired
	private ScriptEngineExecutor scriptEngineExecutor;
	@Value("${work.directory}")
	private String workDirectory;

	private ApplicationContext applicationContext;
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private Map<MetricsCollectType, IMetricsCollector> metricsCollectors;
	private Map<MetricsType, IMetricsParser> metricsParsers;

	@PostConstruct
	public void init() {
		{
			metricsCollectors = Maps.newHashMap();
			Map<String, IMetricsCollector> beans = applicationContext.getBeansOfType(IMetricsCollector.class);
			for (IMetricsCollector bean : beans.values()) {
				metricsCollectors.put(bean.type(), bean);
			}
		}
		{
			metricsParsers = Maps.newHashMap();
			Map<String, IMetricsParser> beans = applicationContext.getBeansOfType(IMetricsParser.class);
			for (IMetricsParser bean : beans.values()) {
				metricsParsers.put(bean.type(), bean);
			}
		}
	}

	@Override
	public Class<? extends BaseMessage> supportType() {
		return TaskScheduleMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		TaskScheduleMessage taskMessage = (TaskScheduleMessage) message;
		try {
			File repoDir = new File(workDirectory, "repository");
			File commitDir = new File(repoDir, taskMessage.getCommitId());
			if (commitDir.exists() == false) {
				TaskMetricsMessage taskMetricsMessage = new TaskMetricsMessage(ClientDetector.CLIENT,
						message.getExchangeId(), taskMessage.getServerMsgId());
				taskMetricsMessage.setStatus(ExecuteStatus.REPO_NEED_UPDATE);
				taskMetricsMessage.setErrorMessage("Repository need update");
				return Arrays.asList(taskMetricsMessage);
			}
			String script = null;
			switch (taskMessage.getScriptContentType()) {
			case TEXT:
				script = taskMessage.getScript();
				break;
			case FILE: {
				File scriptFile = new File(commitDir, taskMessage.getScript());
				script = FileUtils.readFileToString(scriptFile, UTF_8);
			}
				break;
			default:
				throw new RuntimeException("Unsupport script content type " + taskMessage.getScriptContentType());
			}
			Map<String, Object> env = Maps.newHashMap();
			env.putAll(taskMessage.getLabels());
			env.putAll(taskMessage.getParams());
			{
				File metricsDir = new File(workDirectory, "metrics");
				if (metricsDir.exists() == false)
					metricsDir.mkdirs();
				File metricsFile = new File(metricsDir, message.getExchangeId());
				env.put(FileMetricsCollector.METRICS_FILE_LOCATION_FORMAT1, metricsFile.getAbsoluteFile());
				env.put(FileMetricsCollector.METRICS_FILE_LOCATION_FORMAT2, metricsFile.getAbsoluteFile());
			}

			Future<EvalResult> future = executorService.submit(new ScriptTask(scriptEngineExecutor,
					taskMessage.getLanguage(), script, env, taskMessage.isCollectStdLog()));
			EvalResult evalResult = future.get(taskMessage.getTimeout(), TimeUnit.SECONDS);
			IMetricsCollector metricsCollector = metricsCollectors.get(taskMessage.getMetricsCollectType());
			if (metricsCollector == null) {
				throw new RuntimeException("No such metrics collect type " + taskMessage.getMetricsCollectType());
			}
			String metrics = metricsCollector.collect(taskMessage, evalResult, env);
			IMetricsParser metricsParser = metricsParsers.get(taskMessage.getMetricsType());
			if (metricsParser == null) {
				throw new RuntimeException("No such metrics type " + taskMessage.getMetricsType());
			}
			List<BaseMessage> messages = Lists.newArrayList();
			if (taskMessage.isCollectStdLog()) {
				TaskLogMessage taskLogMessage = new TaskLogMessage(ClientDetector.CLIENT, message.getExchangeId(),
						taskMessage.getServerMsgId());
				if (evalResult.getStdout() != null) {
					taskLogMessage.setOut(evalResult.getStdout().toString());
				}
				if (evalResult.getStderr() != null) {
					taskLogMessage.setErr(evalResult.getStderr().toString());
				}
				messages.add(taskLogMessage);
			}
			List<TaskMetricsMessage> metricsMessage = metricsParser.parse(taskMessage, metrics);
			messages.addAll(metricsMessage);
			return messages;
		} catch (Exception e) {
			TaskMetricsMessage taskMetricsMessage = new TaskMetricsMessage(ClientDetector.CLIENT,
					message.getExchangeId(), taskMessage.getServerMsgId());
			taskMetricsMessage.setStatus(ExecuteStatus.FAILED);
			taskMetricsMessage
					.setErrorMessage(String.format("Exception %s reason %s", e.getClass().getName(), e.getMessage()));
			return Arrays.asList(taskMetricsMessage);
		}
	}

	public static class ScriptTask implements Callable<EvalResult> {

		private ScriptEngineExecutor scriptEngineExecutor;

		private String language;
		private String script;
		private Map<String, Object> env;
		private boolean collectStdLog;

		public ScriptTask(ScriptEngineExecutor scriptEngineExecutor, String language, String script,
				Map<String, Object> env, boolean collectStdLog) {
			super();
			this.scriptEngineExecutor = scriptEngineExecutor;
			this.language = language;
			this.script = script;
			this.env = env;
			this.collectStdLog = collectStdLog;
		}

		@Override
		public EvalResult call() throws Exception {
			try {
				return scriptEngineExecutor.eval(language, script, env, collectStdLog);
			} catch (Exception e) {
				EvalResult evalResult = new EvalResult();
				StringWriter stringWriter = new StringWriter();
				stringWriter.write("Exception " + e.getClass().getName() + " reason " + e.getMessage());
				evalResult.setStderr(stringWriter);
				return evalResult;
			}
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
