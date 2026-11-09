package br.com.memdeck;

import org.springframework.boot.SpringApplication;

public class TestMemdeckBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(MemdeckBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
