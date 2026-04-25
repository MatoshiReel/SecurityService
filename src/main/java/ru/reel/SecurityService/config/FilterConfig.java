package ru.reel.SecurityService.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.reel.SecurityService.filter.AesDecryptionFilter;

@Configuration
public class FilterConfig {
    private final HandlerExceptionResolver resolver;
    @Value("${ENC_AES_SECRET_KEY}")
    private String secretKeyAES;
    @Value("${ENC_AES_SALT}")
    private String saltAES;

    public FilterConfig(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Bean
    public FilterRegistrationBean<AesDecryptionFilter> aesDecryptionFilter() {
        FilterRegistrationBean<AesDecryptionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AesDecryptionFilter(resolver, secretKeyAES, saltAES));
        registrationBean.setOrder(1);
        registrationBean.setEnabled(true);
        registrationBean.addUrlPatterns("/security/change/password", "/security/change/login");
        return registrationBean;
    }
}
