package com.louyj.cottus.worker.sshd;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

/**
 *
 * Create at 2020年6月30日
 *
 * @author Louyj
 *
 */
public class SshClient1 {

	public static ClientChannel channel;

	public static void main(String[] args) throws Exception {
		new Worker().start();
		Thread.sleep(10000);
		channel.getInvertedIn().write("ls\r\n".getBytes());
		channel.getInvertedIn().flush();
		TimeUnit.SECONDS.sleep(2);

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
						channel.setIn(System.in);
						channel.setOut(System.out);
						channel.setErr(System.err);
						try {
							channel.open().verify(1000);
							SshClient1.channel = channel;
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
