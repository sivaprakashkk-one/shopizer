package com.salesmanager.shop.application.config;

import com.salesmanager.shop.utils.AWSFilePathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"aws", "cloud"})
public class AwsLocationImageConfig {

  @Value("${contentUrl}")
  private String contentUrl;

  @Bean
  public AWSFilePathUtils img() {
    AWSFilePathUtils awsFilePathUtils = new AWSFilePathUtils();
    awsFilePathUtils.setBasePath(contentUrl);
    return awsFilePathUtils;
  }
}
