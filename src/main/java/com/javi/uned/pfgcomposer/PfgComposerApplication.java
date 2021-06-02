package com.javi.uned.pfgcomposer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PfgComposerApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PfgComposerApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

}
