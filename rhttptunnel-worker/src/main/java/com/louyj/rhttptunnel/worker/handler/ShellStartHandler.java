package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellStartMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShellStartHandler implements IMessageHandler {

	@Value("${work.directory}")
	private String workDirectory;

	private String shell = "tail -f {{in}} | while read line\n" + "do\n" + " eval \" ${line}\" > {{out}} 2>{{err}}\n"
			+ " echo \"${line}\" > {{res}}\n" + "done\n" + "";

	private Map<String, ExecuteResultHandler> execHandlers = Maps.newConcurrentMap();

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellStartMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		File infile = new File(workDirectory, "temp/" + message.getExchangeId() + ".in");
		File outfile = new File(workDirectory, "temp/" + message.getExchangeId() + ".out");
		File resfile = new File(workDirectory, "temp/" + message.getExchangeId() + ".res");
		File scriptfile = new File(workDirectory, "temp/" + message.getExchangeId() + ".sc");
		File errFile = new File(workDirectory, "temp/" + message.getExchangeId() + ".err");

		infile.getParentFile().mkdirs();
		infile.createNewFile();
		outfile.createNewFile();
		resfile.createNewFile();
		scriptfile.createNewFile();
		errFile.createNewFile();

		String inpath = infile.getAbsolutePath();
		String outpath = outfile.getAbsolutePath();
		String respath = resfile.getAbsolutePath();
		String scpath = scriptfile.getAbsolutePath();
		String errpath = errFile.getAbsolutePath();
		String script = shell.replace("{{in}}", inpath).replace("{{out}}", outpath).replace("{{res}}", respath)
				.replace("{{err}}", errpath);
		IOUtils.write(script, new FileOutputStream(scriptfile));
		String cmdLine = "/bin/sh " + scpath;

		ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.HOURS.toMillis(1));
		ExecuteResultHandler handler = new DefaultExecuteResultHandler();
		CommandLine commandline = CommandLine.parse(cmdLine);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWatchdog(watchdog);
		executor.execute(commandline, handler);
		execHandlers.put(message.getClient().identify(), handler);
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()).withMessage("Worker ready"));
	}

}
