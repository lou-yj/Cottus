package com.louyj.rhttptunnel.client.extend;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.core.MethodParameter;
import org.springframework.shell.Availability;
import org.springframework.shell.CommandNotCurrentlyAvailable;
import org.springframework.shell.CommandNotFound;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.ParameterValidationException;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;
import org.springframework.shell.Utils;
import org.springframework.util.ReflectionUtils;

import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.annotation.CommandGroups;
import com.louyj.rhttptunnel.client.exception.NoPermissionException;
import com.louyj.rhttptunnel.model.http.ExchangeContext;
import com.louyj.rhttptunnel.model.http.MessageExchanger;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
@SuppressWarnings("rawtypes")
public class CustomShell extends Shell {

	private final ResultHandler syncHandler;
	private Validator validatorx;
	private ClientSession clientSession;
	private MessageExchanger messageExchanger;

	public CustomShell(ResultHandler resultHandler, ClientSession clientSession, MessageExchanger messageExchanger) {
		super(resultHandler);
		this.syncHandler = resultHandler;
		this.clientSession = clientSession;
		this.messageExchanger = messageExchanger;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(InputProvider inputProvider) throws IOException {
		Object result = null;
		while (!(result instanceof ExitRequest)) {
			Input input;
			try {
				input = inputProvider.readInput();
			} catch (ExitRequest e) {
				continue;
			} catch (Exception e) {
				syncHandler.handleResult(e);
				continue;
			}
			if (input == null) {
				break;
			}
			result = evaluate(input);
			if (result != NO_INPUT && !(result instanceof ExitRequest)) {
				syncHandler.handleResult(result);
			}
		}
	}

	public Object evaluate(Input input) {
		if (noInput(input)) {
			return NO_INPUT;
		}

		String line = input.words().stream().collect(Collectors.joining(" ")).trim();
		String command = findLongestCommand(line);

		List<String> words = input.words();
		if (command != null) {
			MethodTarget methodTarget = methodTargets.get(command);
			Availability availability = methodTarget.getAvailability();
			if (availability.isAvailable()) {
				List<String> wordsForArgs = wordsForArguments(command, words);
				Method method = methodTarget.getMethod();
				try {
					Object[] args = resolveArgs(method, wordsForArgs);
					validateArgs(args, methodTarget);
					checkPermission(methodTarget.getBean().getClass(), method, command, args);
					return ReflectionUtils.invokeMethod(method, methodTarget.getBean(), args);
				} catch (Exception e) {
					return e;
				}
			} else {
				return new CommandNotCurrentlyAvailable(command, availability);
			}
		} else {
			return new CommandNotFound(words);
		}
	}

	public void checkPermission(Class<?> clazz, Method method, String command, Object[] args) {
		ExchangeContext exchangeContext = new ExchangeContext();
		exchangeContext.setClientId(ClientDetector.CLIENT.identify());
		exchangeContext.setCommand(command);
		exchangeContext.setClassName(clazz.getName());
		exchangeContext.setMethodName(method.getName());
		exchangeContext.setArgs(args);
		CommandGroups commandGroups = method.getAnnotation(CommandGroups.class);
		if (commandGroups != null)
			exchangeContext.setCommandGroups(commandGroups.value());
		clientSession.setExchangeContext(exchangeContext);
		messageExchanger.getExchangeContext().set(exchangeContext);
		if (clientSession.isSuperAdmin() == false && exchangeContext.isAllowAll() == false
				&& clientSession.hasPermission(command) == false) {
			throw new NoPermissionException();
		}
	}

	private boolean noInput(Input input) {
		return input.words().isEmpty() || (input.words().size() == 1 && input.words().get(0).trim().isEmpty())
				|| (input.words().iterator().next().matches("\\s*//.*"));
	}

	private String findLongestCommand(String prefix) {
		String result = methodTargets.keySet().stream()
				.filter(command -> prefix.equals(command) || prefix.startsWith(command + " "))
				.reduce("", (c1, c2) -> c1.length() > c2.length() ? c1 : c2);
		return "".equals(result) ? null : result;
	}

	private List<String> wordsForArguments(String command, List<String> words) {
		int wordsUsedForCommandKey = command.split(" ").length;
		List<String> args = words.subList(wordsUsedForCommandKey, words.size());
		int last = args.size() - 1;
		if (last >= 0 && "".equals(args.get(last))) {
			args.remove(last);
		}
		return args;
	}

	private Object[] resolveArgs(Method method, List<String> wordsForArgs) {
		List<MethodParameter> parameters = Utils.createMethodParameters(method).collect(Collectors.toList());
		Object[] args = new Object[parameters.size()];
		Arrays.fill(args, UNRESOLVED);
		for (ParameterResolver resolver : parameterResolvers) {
			for (int argIndex = 0; argIndex < args.length; argIndex++) {
				MethodParameter parameter = parameters.get(argIndex);
				if (args[argIndex] == UNRESOLVED && resolver.supports(parameter)) {
					args[argIndex] = resolver.resolve(parameter, wordsForArgs).resolvedValue();
				}
			}
		}
		return args;
	}

	private void validateArgs(Object[] args, MethodTarget methodTarget) {
		for (int i = 0; i < args.length; i++) {
			if (args[i] == UNRESOLVED) {
				MethodParameter methodParameter = Utils.createMethodParameter(methodTarget.getMethod(), i);
				throw new IllegalStateException("Could not resolve " + methodParameter);
			}
		}
		if (validatorx == null) {
			try {
				Field declaredField = Shell.class.getDeclaredField("validator");
				declaredField.setAccessible(true);
				validatorx = (Validator) declaredField.get(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		Set<ConstraintViolation<Object>> constraintViolations = validatorx.forExecutables()
				.validateParameters(methodTarget.getBean(), methodTarget.getMethod(), args);
		if (constraintViolations.size() > 0) {
			throw new ParameterValidationException(constraintViolations, methodTarget);
		}
	}

}
