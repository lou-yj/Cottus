package com.louyj.rhttptunnel.worker.shell;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.louyj.rhttptunnel.worker.shell.ShellWrapper.ShellOutput;
import com.louyj.rhttptunnel.worker.shell.ShellWrapper.SubmitStatus;

/**
 *
 * Create at 2020年7月1日
 *
 * @author Louyj
 *
 */
public class ShellWraperTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		ShellWrapper shellWrapper = new ShellWrapper();
		shellWrapper.setup();
		Scanner sc = new Scanner(System.in);
		String prompt = "shell:> ";
		while (true) {
			System.out.print(prompt);
			String line = sc.nextLine();
			if (line == null) {
				break;
			}
			Pair<SubmitStatus, String> submit = shellWrapper.submit(line);
			if (submit.getLeft() != SubmitStatus.SUCCESS) {
				System.out.println("Bad status " + submit.getLeft());
				break;
			}
			String cmdId = submit.getRight();
			while (true) {
				ShellOutput fetchResult = shellWrapper.fetchResult(cmdId);
//				System.out.println("fetch result");
				if (isNotEmpty(fetchResult.out)) {
					System.out.println(StringUtils.join(fetchResult.out, "\n"));
				}
				if (isNotEmpty(fetchResult.err)) {
					System.err.println(StringUtils.join(fetchResult.err, "\n"));
				}
				if (fetchResult.finished) {
					break;
				}
				TimeUnit.SECONDS.sleep(1);
			}
		}
		sc.close();
	}

}
