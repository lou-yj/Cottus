package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShellHandler implements IMessageHandler {

	private static Logger logger = LoggerFactory.getLogger(ShellHandler.class);

	@Autowired
	private MessageExchanger messageExchanger;

//	private Map<String, ExchangeThread> threads = Maps.newConcurrentMap();

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
//		ExchangeThread workerThread = threads.get(message.getClient().identify());
//		if (workerThread == null) {
//			return Lists.newArrayList(RejectMessage.creason(CLIENT, message.getExchangeId(), "Sesson not found"));
//		}
//		workerThread.queue.put((ShellMessage) message);
//		return null;
	}

	public BaseMessage startShell(BaseMessage message) {
//		ExchangeThread exchangeThread = new ExchangeThread();
//		exchangeThread.identify = message.getClient().identify();
//		exchangeThread.exchangeId = message.getExchangeId();
//		threads.put(message.getClient().identify(), exchangeThread);
//		WorkerThread worker = new WorkerThread();
//		worker.exchangeThread = exchangeThread;
//		worker.start();
//		return null;
	}

	public BaseMessage endShell(BaseMessage message) {
//		String identify = message.getClient().identify();
//		ExchangeThread thread = threads.get(identify);
//		if (thread != null) {
//			thread.shouldBreak = true;
//		}
//		return AckMessage.cack(CLIENT, message.getExchangeId());
	}

//	public class ExchangeThread extends Thread {
//
//		private OutputStream shellIn;
//		private InputStream shellOut;
//		private InputStream shellErr;
//		private ClientChannel channel;
//		private String identify;
//		private String exchangeId;
//
//		private BlockingDeque<ShellMessage> queue = new LinkedBlockingDeque<>(100);
//
//		private boolean shouldBreak = false;
//
//		@Override
//		public void run() {
//			while (!shouldBreak) {
//				try {
//					ShellMessage shellMessage = queue.poll(1, TimeUnit.SECONDS);
//					if (shellMessage == null) {
//						continue;
//					}
////					pumpInput(shellMessage);
////					if (!pumpOutput(shellMessage, shellOut)) {
////						if (!pumpOutput(shellMessage, shellErr)) {
////							System.out.println("Send empty resp");
////							ShellMessage resp = new ShellMessage(CLIENT, shellMessage.getExchangeId());
////							resp.setMessage("");
////							messageExchanger.jsonPost(WORKER_EXCHANGE, resp);
////						}
////					}
//					ByteArrayOutputStream baos = new ByteArrayOutputStream();
//					ByteArrayInputStream bais = new ByteArrayInputStream(shellMessage.getMessage().getBytes("utf8"));
//					channel.setOut(baos);
//					channel.setErr(baos);
//					channel.setIn(bais);
//					System.out.println("xxx" + baos.toString("utf8"));
//				} catch (Exception e) {
//					logger.error("", e);
//				}
//			}
//			IOUtils.closeQuietly(channel);
//			threads.remove(identify);
//		}
//
//		private void pumpInput(ShellMessage message) throws IOException {
//			String content = message.getMessage();
//			shellIn.write(content.getBytes(UTF_8));
//			shellIn.flush();
//		}
//
//		private boolean pumpOutput(ShellMessage message, InputStream in) throws IOException {
//			int available = in.available();
//			System.out.println("available " + available);
//			if (available > 0) {
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				byte[] bs = new byte[1024];
//				while (available > 0) {
//					int len = in.read(bs);
//					System.out.println("Read " + len);
//					if (len > 0) {
//						baos.write(bs, 0, len);
//					} else {
//						break;
//					}
//					available = in.available();
//				}
//				String output = new String(baos.toByteArray(), "utf8");
//				System.out.println("Send shell resp: " + output);
//				ShellMessage shellMessage = new ShellMessage(CLIENT, message.getExchangeId());
//				shellMessage.setMessage(output);
//				messageExchanger.jsonPost(WORKER_EXCHANGE, shellMessage);
//				return true;
//			} else {
//				return false;
//			}
//		}
//	}
//
//	public class WorkerThread extends Thread {
//
//		private ExchangeThread exchangeThread;
//
//		@Override
//		public void run() {
//			try {
//				doRun();
//			} catch (Exception e) {
//				logger.error("", e);
//				messageExchanger.jsonPost(WORKER_EXCHANGE,
//						RejectMessage.creason(CLIENT, exchangeThread.exchangeId, "Start Shell Failed."));
//			}
//		}
//
//		private void doRun() throws InterruptedException, IOException {
//			SshClient client = SshClient.setUpDefaultClient();
//			client.start();
//			try (ClientSession session = client.connect("aaa", "localhost", 14567).verify(10000).getSession()) {
//				session.addPasswordIdentity("xx");
//				session.auth().verify(10000);
//				try (ClientChannel channel = session.createShellChannel()) {
//					try {
//						channel.open().verify(10000);
//						exchangeThread.shellIn = channel.getInvertedIn();
//						exchangeThread.shellOut = channel.getInvertedOut();
//						exchangeThread.shellErr = channel.getInvertedErr();
//						exchangeThread.channel = channel;
//						messageExchanger.jsonPost(WORKER_EXCHANGE,
//								AckMessage.cack(CLIENT, exchangeThread.exchangeId).withMessage("Worker ready"));
//						System.out.println("-----");
//						exchangeThread.start();
//						channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);
//					} finally {
//					}
//				}
//			}
//		}
//
//	}

}
