package com.example.algamoney.api.event.listener;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.algamoney.api.event.RecursoCriadoEvent;

// Aqui que vai ser executado o código.
// Houve um evento que é o recurso criado evento
// @Component é reponsável por gerenciar os beans
//Esta anotação faz com que o bean registrado no Spring possa ser utilizado em qualquer bean, seja ele um
//	serviço, um DAO, um controller, etc.
//  Dessa forma, podemos injetar objetos das classes anotadas (por meio do @Autowired, por exemplo).
// Há outras anotações estereotipadas que extendem a anotação @Component como, por exemplo, @Service,
// @Repository e @Controller.
@Component
public class RecursoCriadoListener implements ApplicationListener<RecursoCriadoEvent> {

	@Override
	public void onApplicationEvent(RecursoCriadoEvent recursoCriadoEvent) {
		HttpServletResponse	response = recursoCriadoEvent.getResponse();
		Long codigo = recursoCriadoEvent.getCodigo();
		
		adicionarHeaderLocation(response, codigo);
			
	}

	private void adicionarHeaderLocation(HttpServletResponse response, Long codigo) {
		URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{codigo}")
				.buildAndExpand(codigo).toUri();
		response.setHeader("Location" ,  uri.toASCIIString());
	}
	
	

}
