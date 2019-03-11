package com.walliee.springcloudservicesconnectorvaultheaderissue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@EnableConfigServer
@SpringBootApplication(exclude = { RabbitAutoConfiguration.class})
@EnableAuthorizationServer
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
public class SpringCloudConfigServer extends WebSecurityConfigurerAdapter {

	@Autowired
	private TokenEndpoint tokenEndpoint;

	@PostMapping("/oauth/token")
	public ResponseEntity<OAuth2AccessToken> getAccessToken(Principal principal,
															@RequestParam Map<String, String> parameters)
			throws HttpRequestMethodNotSupportedException {
		return tokenEndpoint.postAccessToken(principal, parameters);
	}

	@EnableResourceServer
	@Configuration
	protected static class ResourceServerConfiguration
			extends ResourceServerConfigurerAdapter {
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/**").authorizeRequests().anyRequest().authenticated();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudConfigServer.class);
	}

}
