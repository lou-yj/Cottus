package com.louyj.rhttptunnel.server.automation;

import java.util.Map;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.Sampler;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class AutomateRule {

	public static class Rule {

		private String name;

		private String expression;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

	}

	public static class Handler {

		private String ruleName;

		private String script;

		private Map<String, String> filters = Maps.newHashMap();

		public String getRuleName() {
			return ruleName;
		}

		public void setRuleName(String ruleName) {
			this.ruleName = ruleName;
		}

		public String getScript() {
			return script;
		}

		public void setScript(String script) {
			this.script = script;
		}

		public Map<String, String> getFilters() {
			return filters;
		}

		public void setFilters(Map<String, String> filters) {
			this.filters = filters;
		}

	}

	private Sampler sampler;

	private Rule rule;

	private Handler handler;

	public Sampler getSampler() {
		return sampler;
	}

	public void setSampler(Sampler sampler) {
		this.sampler = sampler;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

}
