package ru.reel.SecurityService.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.reel.SecurityService.filter.AesDecryptionFilter;
import ru.reel.SecurityService.filter.ApplicationJsonContentTypeFilter;

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
        registrationBean.addUrlPatterns(
                "/security/password/change",
                "/security/login/change",
                "/security/email/change",
                "/security/email/send/token",
                "/security/email/send/otp",
                "/security/email/2fa");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ApplicationJsonContentTypeFilter> applicationJsonContentTypeFilter() {
        FilterRegistrationBean<ApplicationJsonContentTypeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApplicationJsonContentTypeFilter());
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns(
                "/security/password/change",
                "/security/login/change",
                "/security/email/send/token");
        return registrationBean;
    }
}
