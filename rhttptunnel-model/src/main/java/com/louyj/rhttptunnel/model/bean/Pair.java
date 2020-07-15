package com.louyj.rhttptunnel.model.bean;

/**
 *
 * Create at 2020年7月15日
 *
 * @author Louyj
 *
 */
public class Pair<L, R> {

	private L left;

	private R right;

	public static <L, R> Pair<L, R> of(L left, R right) {
		Pair<L, R> pair = new Pair<L, R>();
		pair.left = left;
		pair.right = right;
		return pair;
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}

}
