package ru.reel.SecurityService.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

public class ApplicationJsonContentTypeFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        MutableContentTypeHttpRequestWrapper requestWrapper = new MutableContentTypeHttpRequestWrapper((HttpServletRequest) servletRequest, MediaType.APPLICATION_JSON_VALUE);
        filterChain.doFilter(requestWrapper, (HttpServletResponse) servletResponse);
    }
}
