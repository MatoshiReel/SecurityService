package ru.reel.SecurityService.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MutableContentTypeHttpRequestWrapper extends HttpServletRequestWrapper {
    private final String contentType;

    public MutableContentTypeHttpRequestWrapper(HttpServletRequest request, String contentType) {
        super(request);
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getHeader(String name) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            return contentType;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            return Collections.enumeration(List.of(contentType));
        }
        return super.getHeaders(name);
    }
}
