package com.louyj.rhttptunnel.model.bean.automate;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class Alarmer {

	static final Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", CASE_INSENSITIVE | DOTALL);
	static final String tagMain = "@Tag(name='alarm',value='main')";

	private String name;

	private String expression;

	private List<String> groupKeys = Lists.newArrayList();

	public List<String> getGroupKeys() {
		return groupKeys;
	}

	public void setGroupKeys(List<String> groupKeys) {
		this.groupKeys = groupKeys;
	}

	public static Pattern getCommentpattern() {
		return commentPattern;
	}

	public static String getTagmain() {
		return tagMain;
	}

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

	public void check(File ruleFile) {
		if (StringUtils.isAnyBlank(name, expression)) {
			String message = String.format("Bad format for alarmer %s in file %s", name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		parseEpls(ruleFile);
	}

	public List<String> parseEpls(File ruleFile) {
		String ruleFileName = null;
		if (ruleFile != null) {
			ruleFileName = ruleFile.getName();
		}
		String epls = commentPattern.matcher(expression).replaceAll("");
		epls = epls.replaceAll("//.*", "");
		String[] sqls = epls.split(";");
		List<String> result = Lists.newArrayList();
		if (sqls.length == 1) {
			String sql = sqls[0];
			if (sql.contains(tagMain) == false) {
				sql = tagMain + "\n" + sql;
			}
			result.add(sql);
		} else {
			int tagCount = 0;
			for (String sql : sqls) {
				if (sql.contains(tagMain)) {
					tagCount++;
				}
				result.add(sql);
			}
			if (tagCount < 1) {
				String message = String.format("No main entry for alarmer expression %s in file %s", name,
						ruleFileName);
				throw new IllegalArgumentException(message);
			}
			if (tagCount < 1) {
				String message = String.format("Too many main entry for alarmer expression %s in file %s", name,
						ruleFileName);
				throw new IllegalArgumentException(message);
			}
		}
		return result;
	}

}
