package com.louyj.rhttptunnel.server.service.worker;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.louyj.rhttptunnel.model.exception.BadRequestException;
import com.louyj.rhttptunnel.model.message.FileDataMessage;
import com.louyj.rhttptunnel.server.TokenIssuer;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@RestController
public class ExchangeService {

	@Autowired
	private TokenIssuer tokenIssuer;

	@Value("${work.directory}")
	private String workDirectory;

	@RequestMapping(value = "exchange", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_OCTET_STREAM_VALUE)
	public void getfile(HttpServletResponse response, @RequestBody FileDataMessage message) throws IOException {
		File file = new File(workDirectory, message.getFileName());
		if (file.exists() == false || file.isFile() == false) {
			throw new BadRequestException("file not exist " + file.getAbsolutePath());
		}
		FileInputStream fis = new FileInputStream(file);
		ServletOutputStream outputStream = response.getOutputStream();
		IOUtils.copyLarge(fis, outputStream);
	}

}
