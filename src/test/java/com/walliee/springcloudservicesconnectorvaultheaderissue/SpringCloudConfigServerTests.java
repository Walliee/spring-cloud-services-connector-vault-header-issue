package com.walliee.springcloudservicesconnectorvaultheaderissue;

import io.pivotal.spring.cloud.service.config.ConfigClientOAuth2ResourceDetails;
import io.pivotal.spring.cloud.service.config.PlainTextConfigClient;
import io.pivotal.spring.cloud.service.config.PlainTextConfigClientAutoConfiguration;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringCloudConfigServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.profiles.active=vault,plaintext,native", "spring.cloud.config.enabled=true", "eureka.client.enabled=false",
		"spring.cloud.config.client.oauth2.client-id=acme"})
public class SpringCloudConfigServerTests {

	// @formatter:off
	private static final String nginxConfig = "server {\n"
			+ "    listen              80;\n"
			+ "    server_name         example.com;\n"
			+ "}";
	// @formatter:on

	@LocalServerPort
	private int port;

	@Autowired
	private ConfigClientOAuth2ResourceDetails resource;

	@Autowired
	private ConfigClientProperties configClientProperties;

	private PlainTextConfigClient configClient;

	@Before
	public void setup() {
		resource.setAccessTokenUri("http://localhost:" + port + "/oauth/token");
		configClientProperties.setName("app");
		configClientProperties.setProfile(null);
		configClientProperties.setUri(new String[] {"http://localhost:" + port});
		configClient = new PlainTextConfigClientAutoConfiguration()
				.plainTextConfigClient(resource, configClientProperties);
	}

	@Test
	public void shouldFindSimplePlainFile() throws IOException {
		try {
			read(configClient.getConfigFile("nginx.conf"));
		} catch (HttpClientErrorException e) {
			assertEquals(e.getClass(), HttpClientErrorException.BadRequest.class);
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(e.getMessage());
			assertEquals("Missing required header: X-Config-Token", jsonNode.get("message").asText());
		}
	}

	public String read(Resource resource) {
		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(resource.getInputStream()))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
