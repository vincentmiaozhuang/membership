package com.membership.external.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyConfig apiKeyConfig;

    public ApiKeyFilter(ApiKeyConfig apiKeyConfig) {
        this.apiKeyConfig = apiKeyConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 如果没有配置API Key列表，则跳过验证（开发环境）
        if (apiKeyConfig.getKeys() == null || apiKeyConfig.getKeys().isEmpty()) {
            logger.debug("未配置API Key列表，跳过API Key验证 - IP: {}, 路径: {}", getClientIp(request), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("API Key未提供 - IP: {}, 路径: {}", getClientIp(request), request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "API Key未提供");
            return;
        }
        
        if (!apiKeyConfig.isValidApiKey(apiKey)) {
            logger.warn("无效的API Key - IP: {}, 路径: {}", getClientIp(request), request.getRequestURI());
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "无效的API Key");
            return;
        }
        
        logger.debug("API Key验证通过 - IP: {}, 路径: {}", getClientIp(request), request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", status.value());
        error.put("message", message);
        error.put("data", null);
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(error));
    }
}