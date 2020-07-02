package com.louyj.rhttptunnel.client;

import java.io.PrintStream;
import java.util.Locale;

import com.louyj.rhttptunnel.model.message.ClientInfo;

public class CustromPrintStream extends PrintStream {

	private boolean first = true;

	private ClientInfo clientInfo;

	public CustromPrintStream(ClientInfo clientInfo) {
		super(System.out);
		this.clientInfo = clientInfo;
	}

	private void printWorker() {
		if (first) {
			first = false;
			this.println("====>from worker " + clientInfo.getHost() + "<====");
		}
	}

	@Override
	public void print(boolean b) {
		printWorker();
		super.print(b);
	}

	@Override
	public void print(char c) {
		printWorker();
		super.print(c);
	}

	@Override
	public void print(int i) {
		printWorker();
		super.print(i);
	}

	@Override
	public void print(long l) {
		printWorker();
		super.print(l);
	}

	@Override
	public void print(float f) {
		printWorker();
		super.print(f);
	}

	@Override
	public void print(double d) {
		printWorker();
		super.print(d);
	}

	@Override
	public void print(char[] s) {
		printWorker();
		super.print(s);
	}

	@Override
	public void print(String s) {
		printWorker();
		super.print(s);
	}

	@Override
	public void print(Object obj) {
		printWorker();
		super.print(obj);
	}

	@Override
	public void println() {
		printWorker();
		super.println();
	}

	@Override
	public void println(boolean x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(char x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(int x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(long x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(float x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(double x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(char[] x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(String x) {
		printWorker();
		super.println(x);
	}

	@Override
	public void println(Object x) {
		printWorker();
		super.println(x);
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		printWorker();
		return super.printf(format, args);
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		printWorker();
		return super.printf(l, format, args);
	}

}
