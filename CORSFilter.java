package com.topicallocation.topic.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

//@Component
public class CORSFilter implements Filter {

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		/*
		 * HttpServletResponse httpResponse = (HttpServletResponse) res;
		 * httpResponse.setHeader("Access-Control-Allow-Origin",
		 * "http://localhost:4200");
		 * httpResponse.setHeader("Access-Control-Allow-Methods",
		 * "POST, GET, PUT, OPTIONS, DELETE");
		 * httpResponse.setHeader("Access-Control-Allow-Headers",
		 * "Authorization, Content-Type");
		 * //httpResponse.setHeader("Access-Control-Expose-Headers",
		 * "custom-header1, custom-header2");
		 * httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
		 * httpResponse.setHeader("Access-Control-Max-Age", "4800");
		 * System.out.println("---CORS Configuration Completed---"); chain.doFilter(req,
		 * res);
		 */
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}

}