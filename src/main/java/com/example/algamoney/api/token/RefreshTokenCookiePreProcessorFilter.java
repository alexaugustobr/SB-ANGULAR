package com.example.algamoney.api.token;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.catalina.util.ParameterMap;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)// Aqui é pra ter preferência
public class RefreshTokenCookiePreProcessorFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	  throws IOException, ServletException {

	HttpServletRequest req = (HttpServletRequest) request;

	if ("/oauth/token".equalsIgnoreCase(req.getRequestURI())
	    && "refresh_token".equals(req.getParameter("grant_type"))
	    && req.getCookies() != null) {

	  String refreshToken = 
	      Stream.of(req.getCookies())
	          .filter(cookie -> "refreshToken".equals(cookie.getName()))
	          .findFirst()
	          .map(cookie -> cookie.getValue())
	          .orElse(null);

	  req = new MyServletRequestWrapper(req, refreshToken);
	}
    // depois que passar a requisição é necessário continuar com a cadeia do filtro que seria a resposta
	chain.doFilter(req, response);
	
	}
	
	// Depois que a requisição é mandada não dá mais pra alterar ... no entanto,
	// se fizer esse classe abaixo é possível.
	 static class MyServletRequestWrapper extends HttpServletRequestWrapper {

			private String refreshToken;

			public MyServletRequestWrapper(HttpServletRequest request, String refreshToken) {
				super(request);
				this.refreshToken = refreshToken;
			}

			@Override
			public Map<String, String[]> getParameterMap() {
				ParameterMap<String, String[]> map = new ParameterMap<>(getRequest().getParameterMap());
				// Esse refreshToken abaixo é recuperado do cookie
				map.put("refresh_token", new String[] { refreshToken });// Esse 
				// Aqui é para travar o mapa da requisição depois que pegar oo parâmetro desejado
				map.setLocked(true);
				return map;
			}

		}
		
	}


