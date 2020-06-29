package com.louyj.rhttptunnel.client.extend;

import java.util.Collection;

import javax.validation.Validation;
import javax.validation.Validator;

import org.jline.reader.LineReader;
import org.jline.reader.Parser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.result.ResultHandlerConfig;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
@SuppressWarnings("rawtypes")
@Configuration
@Import(ResultHandlerConfig.class)
public class JlineConfiguration {

	@Bean
	public CustomShell customShell(@Qualifier("main") ResultHandler resultHandler,
			AsyncShellResultHanlder asyncShellResultHanlder) {
		return new CustomShell(resultHandler, asyncShellResultHanlder);
	}

	@Bean
	public ApplicationRunner customApplicationRunner(LineReader lineReader, PromptProvider promptProvider,
			Parser parser, CustomShell shell, ConfigurableEnvironment environment) {
		return new CustomShellApplicationRunner(lineReader, promptProvider, parser, shell, environment);
	}

	@Bean
	@Qualifier("spring-shell")
	public ConversionService shellConversionService(ApplicationContext applicationContext) {
		Collection<Converter> converters = applicationContext.getBeansOfType(Converter.class).values();
		Collection<GenericConverter> genericConverters = applicationContext.getBeansOfType(GenericConverter.class)
				.values();
		Collection<ConverterFactory> converterFactories = applicationContext.getBeansOfType(ConverterFactory.class)
				.values();

		DefaultConversionService defaultConversionService = new DefaultConversionService();
		for (Converter converter : converters) {
			defaultConversionService.addConverter(converter);
		}
		for (GenericConverter genericConverter : genericConverters) {
			defaultConversionService.addConverter(genericConverter);
		}
		for (ConverterFactory converterFactory : converterFactories) {
			defaultConversionService.addConverterFactory(converterFactory);
		}
		return defaultConversionService;
	}

	@Bean
	@ConditionalOnMissingBean(Validator.class)
	public Validator validator() {
		return Validation.buildDefaultValidatorFactory().getValidator();
	}

}
