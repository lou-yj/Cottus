package com.louyj.rhttptunnel.client.extend;

import java.util.List;
import java.util.stream.Collectors;

import org.jline.reader.ParsedLine;
import org.springframework.shell.Input;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
public class ParsedLineInput implements Input {

	private final ParsedLine parsedLine;

	ParsedLineInput(ParsedLine parsedLine) {
		this.parsedLine = parsedLine;
	}

	@Override
	public String rawText() {
		return parsedLine.line();
	}

	@Override
	public List<String> words() {
		return sanitizeInput(parsedLine.words());
	}

	static List<String> sanitizeInput(List<String> words) {
		words = words.stream().map(s -> s.replaceAll("^\\n+|\\n+$", "")) // CR at beginning/end of line introduced by
																			// backslash continuation
				.map(s -> s.replaceAll("\\n+", " ")) // CR in middle of word introduced by return inside a quoted string
				.collect(Collectors.toList());
		return words;
	}
}
