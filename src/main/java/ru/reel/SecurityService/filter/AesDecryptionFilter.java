package ru.reel.SecurityService.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
public class AesDecryptionFilter implements Filter {
    private final HandlerExceptionResolver resolver;
    private final String secretKeyAES;
    private final String saltAES;

    public AesDecryptionFilter(HandlerExceptionResolver resolver, String secretKeyAES, String saltAES) {
        this.resolver = resolver;
        this.secretKeyAES = secretKeyAES;
        this.saltAES = saltAES;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String encryptedBody = request.getReader().lines().collect(Collectors.joining());
        String decryptedBody = "";
        boolean hasError = false;
        if(!encryptedBody.isEmpty()) {
            try {
                decryptedBody = Encryptors.delux(secretKeyAES, saltAES).decrypt(encryptedBody);
            } catch(IllegalArgumentException | IllegalStateException e) {
                hasError = true;
                log.warn("url: auth/signup, Illegal encrypting data : {}", encryptedBody, e);
                resolver.resolveException(request, response, null, new IllegalStateException());
            }
        }
        if(!hasError) {
            MutableBodyHttpRequestWrapper requestWrapper = new MutableBodyHttpRequestWrapper(request, decryptedBody, MediaType.APPLICATION_JSON_VALUE);
            filterChain.doFilter(requestWrapper, response);
        }
    }
}
