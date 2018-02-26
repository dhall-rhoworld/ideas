package com.rho.rhover.web;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Servlet filter that logs username and request
 * @author dhall
 *
 */
@Component
public class UserRequestLoggingFilter implements Filter {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		String url = httpRequest.getRequestURL().toString();
		if (url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png") || url.endsWith(".jpg")) {
			chain.doFilter(request, response);
			return;
		}
		Principal user = httpRequest.getUserPrincipal();
		String userName = "NOT_LOGGED_IN";
		if (user != null) {
			userName = user.getName();
		}
		StringBuilder message = new StringBuilder("USER: " + userName);
		message.append(" | URL: " + url);
		String queryString = httpRequest.getQueryString();
		if (queryString != null && queryString.length() > 0) {
			message.append(" | QUERY_STRING: " + queryString);
		}
		logger.info(message.toString());
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
