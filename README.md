Sample to demonstrate issue with spring-cloud-services-spring-connector's `PlainTextOAuth2ConfigClient`

`X-Config-Token` is mandatory when on all requests to Spring Config Server when Vault is a property source. 
PlainTextOAuth2ConfigClient doesn't set this header and the call fails with the following message: 
```
Missing required header: X-Config-Token
```
https://github.com/pivotal-cf/spring-cloud-services-connector/issues/95
