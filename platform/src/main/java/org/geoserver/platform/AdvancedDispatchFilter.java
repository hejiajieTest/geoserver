/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.platform;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.*;

/**
 * A servlet filter that allows for advanced dispatching. 
 * <p>
 * This fiter allows for a single mapping from web.xml for all requests to the spring dispatcher. 
 * It creates a wrapper around the servlet request object that "fakes" the serveltPath property to 
 * make it look like the mapping was created in web.xml when in actuality it was created in spring.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class AdvancedDispatchFilter implements Filter {

    private Pattern pattern;

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestPath = httpServletRequest.getServletPath();

        //add by yt.chen
        String pathInfo = httpServletRequest.getPathInfo();
        if (StringUtils.length(pathInfo) > 0) {
            requestPath = requestPath + pathInfo;
        }

        if (pattern.matcher(requestPath).find()) {
            chain.doFilter(request, response);
            return;
        }
        // end
        if (request instanceof HttpServletRequest) {
            request = new AdvancedDispatchHttpRequest((HttpServletRequest) request);
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        String ignoreUrl = filterConfig.getInitParameter("ignoreUrl");
        pattern = Pattern.compile(ignoreUrl);
    }

    static class AdvancedDispatchHttpRequest extends HttpServletRequestWrapper {

        String servletPath = null;

        public AdvancedDispatchHttpRequest(HttpServletRequest delegate) {
            super(delegate);

            if (delegate.getClass().getSimpleName().endsWith("MockHttpServletRequest")) {
                return;
            }

            String path = delegate.getPathInfo();

            if (path == null) {
                return;
            }

            int slash = path.indexOf('/', 1);
            if (slash > -1 ) {
                this.servletPath = path.substring(0, slash);
            }
            else {
                this.servletPath = path;
            }

            int question = this.servletPath.indexOf('?');
            if (question > -1 ) {
                this.servletPath = this.servletPath.substring(0, question);
            }

        }

        public String getPathInfo() {
            HttpServletRequest delegate = (HttpServletRequest) getRequest();
            if (servletPath != null &&
                    delegate.getPathInfo() != null &&
                    delegate.getPathInfo().startsWith(servletPath))
                return delegate.getPathInfo().substring(servletPath.length());
            else
                return delegate.getPathInfo();
        }

        public String getServletPath() {
            return servletPath != null ?
                    servletPath : ((HttpServletRequest)getRequest()).getServletPath();
        }
    }

}
