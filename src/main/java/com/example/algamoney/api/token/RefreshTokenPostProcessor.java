package com.example.algamoney.api.token;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.example.algamoney.api.config.property.AlgamoneyApiProperty;

// É um processador depois que o refresh token foi criado.
@SuppressWarnings("deprecation")
@ControllerAdvice
// Um aviso do controller permite que você use exatamente as mesmas técnicas de manipulação de exceções
//mas aplica-las por toda a aplicação, não apenas à controllers individuais. Você pode pensar nelas como
//um interceptador direcionado por anotação
// o OAuth2AccessToken é o tipo de dado que eu quero que seja interceptado.
public class RefreshTokenPostProcessor implements ResponseBodyAdvice<OAuth2AccessToken> {
	
	@Autowired
	private AlgamoneyApiProperty algamoneyApiProperty;

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		
		return returnType.getMethod().getName().equals("postAccessToken");
	}

	@Override// Esse método abaixo só vai ser executado quando o supports for true ou seja
	// quando ele retornar returnType.getMethod().getName().equals("postAccessToken");
	public OAuth2AccessToken beforeBodyWrite(OAuth2AccessToken body, MethodParameter returnType,
			MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		
		// The servlet container creates an HttpServletRequest object andpasses it as an argument to the 
		//servlet's service methods(doGet, doPost, etc).
		HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
		HttpServletResponse resp = ((ServletServerHttpResponse) response).getServletResponse();
		
		// Aqui foi feito um casting pq não é possível usar o setRefreshToken para tirar do body
		DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) body;
		
		// Servlet é uma classe Java usada para estender as funcionalidades de um servidor.
		//Aqui abaixo pegando o refreshToken para adicionar no Cookie
		String refreshToken = body.getRefreshToken().getValue();
		adicionarRefreshTokenNoCookie(refreshToken, req, resp);
		
		removerRefreshTokenDoBody(token);
		
	
		
		return body;
	}

	private void removerRefreshTokenDoBody(DefaultOAuth2AccessToken token) {
		// tira o token do body
		token.setRefreshToken(null);
		
	}

	private void adicionarRefreshTokenNoCookie(String refreshToken, HttpServletRequest req, HttpServletResponse resp) {
		// Criando um cookie e dando um nome de refreshToken
		Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
		// Aqui o cookie só vai funcionar em http
		refreshTokenCookie.setHttpOnly(true);
		// ir para https
		refreshTokenCookie.setSecure(algamoneyApiProperty.getSeguranca().isEnableHttps());// ir para https somente em produção
		// o caminho percorrido
		refreshTokenCookie.setPath(req.getContextPath() + "/oauth/token");
		
		refreshTokenCookie.setMaxAge(259200); // um mês de validade
		// Adiciona o cookie na resposta.
		resp.addCookie(refreshTokenCookie);
		
	}
	
	
	
	

}
