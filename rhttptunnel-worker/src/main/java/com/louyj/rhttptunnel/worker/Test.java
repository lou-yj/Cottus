package com.louyj.rhttptunnel.worker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

/**
 *
 * Created on 2020年3月19日
 *
 * @author Louyj
 *
 */
public class Test {

	public static ClientChannel channel;

	public static void main(String[] args) throws IOException, InterruptedException {
//		new Worker().start();
//		Thread.sleep(10000);
//		channel.getInvertedIn().write("ls\r\n".getBytes());
//		channel.getInvertedIn().flush();
//		TimeUnit.SECONDS.sleep(2);
//
//		channel.addRequestHandler(n);
//		channel.handle
//		
//		int available = channel.getInvertedOut().available();
//		System.out.println(available);
//		System.out.println(IOUtils.toString(channel.getInvertedOut()));

		OutputStream out = new ByteArrayOutputStream();
		String cmdLine = "sh -i";
		CommandLine commandline = CommandLine.parse(cmdLine);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(120000);

		DefaultExecutor executor = new DefaultExecutor();
		PumpStreamHandler streamHandler = new PumpStreamHandler(System.out);
		executor.setStreamHandler(streamHandler);
		executor.getStreamHandler().setProcessInputStream(out);
		executor.setWatchdog(watchdog);
		executor.execute(commandline);
		out.write("ls\n".getBytes());

		TimeUnit.SECONDS.sleep(10000);
	}

	static class Worker extends Thread {

		@Override
		public void run() {
			try {
				SshClient client = SshClient.setUpDefaultClient();
				client.start();
				try (ClientSession session = client.connect("aaa", "localhost", 14567).verify(10000).getSession()) {
					session.addPasswordIdentity("xx");
					session.auth().verify(10000);
					try (ClientChannel channel = session.createShellChannel()) {
//						channel.setIn(System.in);
//						channel.setOut(System.out);
//						channel.setErr(System.err);
						try {
							channel.open().verify(10000);
							Test.channel = channel;
							channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);
						} finally {
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
