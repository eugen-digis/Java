package com.acme.processor.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.acme.aggregator.dao.filter.FilterRepository;
import com.acme.common.mapper.FilterMapper;
import com.acme.processor.service.filter.FilterService;
import com.acme.processor.service.filter.IFilterService;
import java.util.Arrays;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class RestConfig {

  @Bean
  public FilterRegistrationBean corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
    config.setAllowedMethods(Arrays.asList(new String[]{GET.name(), HEAD.name(), POST.name(), PUT.name(), DELETE.name()}));
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
    bean.setOrder(-200);
    return bean;
  }

  @Bean
  public FilterMapper filterMapper() {
    return new FilterMapper();
  }

  @Bean
  public IFilterService filterService(FilterRepository repository, FilterMapper filterMapper) {
    return new FilterService(repository, filterMapper);
  }
}
