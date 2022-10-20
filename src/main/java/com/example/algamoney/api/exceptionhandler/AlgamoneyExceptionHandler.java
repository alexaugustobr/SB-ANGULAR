
package com.example.algamoney.api.exceptionhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice // É um controlador que observa toda a aplicação.
public class AlgamoneyExceptionHandler extends ResponseEntityExceptionHandler{
	
	
	@Autowired
	private MessageSource messageSource;
	
	// Captura uma mensagem que não foi lida.
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		// Aqui existe um método que trata a exceção!
		
		// O null abaixo é porque não tem nenhum parâmetro extra.
		// o local é o atual
		String mensagemUsuario = messageSource.getMessage("mensagem.invalida", null, LocaleContextHolder.getLocale());
		// senão usar o ternário pegando a causa quando diferente de null vai dar exceção.
		String mensagemDesenvolvedor = ex.getCause() != null ? ex.getCause().toString() : ex.toString() ;
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario,mensagemDesenvolvedor));
		return handleExceptionInternal(ex, erros , headers, HttpStatus.BAD_REQUEST, request);
	}
	
	// Método que trata quando os argumentos não são válidos, passou pela validação mas
	// não foi aceito.
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		// No BindingResult é que temos todas as lista de erros.
		List<Erro> erros = criarListaDeErros(ex.getBindingResult());
		return handleExceptionInternal(ex, erros,  headers, HttpStatus.BAD_REQUEST, request);
	}
	
	@ExceptionHandler({EmptyResultDataAccessException.class})// Aqui é um array que passa todas as exceções
	@ResponseStatus(HttpStatus.NOT_FOUND) // estamos retornando objeto não encontrado pois ja foi deletado.
	// Aqui abaixo estamos passando a exceção como parâmetro
	// Abaixo pega uma execeção como parâmetro e uma requisição.
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, WebRequest request) {
		
		String mensagemUsuario = messageSource.getMessage("recurso.nao-encontrado", null, LocaleContextHolder.getLocale());
		//String mensagemDesenvolvedor = ex.getCause().toString();
		// Aqui abaixo não precisa passar a causa pois já é a própria exceção pronta passada na classe.
		String mensagemDesenvolvedor = ex.toString();
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario,mensagemDesenvolvedor));
		
		// Aqui abaixo tem que passar uma nova instância de headers
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
		
		
	}
	
	@ExceptionHandler({DataIntegrityViolationException.class})
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request){
		

		String mensagemUsuario = messageSource.getMessage("recurso.operacao-nao-permitida", null, LocaleContextHolder.getLocale());
		//String mensagemDesenvolvedor = ex.getCause().toString();
		// Aqui abaixo não precisa passar a causa pois já é a própria exceção pronta passada na classe.
		//String mensagemDesenvolvedor = ex.toString();
		// Como adicionamos uma dependencia para tratar de erros no pom.xml temos uma nova forma de tratar erros abaixo
		// substituindo a forma acima pois abaixo temos uma forma mais descritiva , organizada.
		String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
		
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario,mensagemDesenvolvedor));
		
		// Aqui abaixo tem que passar uma nova instância de headers
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
		
	}
	
	
	
	
	
	// Aqui é passado uma lista pois pode ter mais de um erro.
	private List<Erro> criarListaDeErros(BindingResult bindingResult){
		     List<Erro> erros = new ArrayList<>();
		     //método getfieldError tem todos os erros do campo.
		     // bindresult é resultado vinculado. 
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			String mensagemUsuario = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
			// Aqui tem que fazer um casting pra string.
			String mensagemDesenvolvedor = fieldError.toString();
			// adcionando o erro na lista.
			erros.add(new Erro(mensagemUsuario, mensagemDesenvolvedor));
			
		}
		
		
		
		return erros;
	}
	
	// Como essa classe só vai funcionar dentro da Exception... ela foi colocada aqui mesmo.	
	public static class Erro {
		
		private String mensagemUsuario;
		private String mensagemDesenvolvedor;
		
		public Erro(String mensagemUsuario, String mensagemDesenvolvedor) {
		
			this.mensagemUsuario = mensagemUsuario;
			this.mensagemDesenvolvedor = mensagemDesenvolvedor;
		}

		public String getMensagemUsuario() {
			return mensagemUsuario;
		}

		public String getMensagemDesenvolvedor() {
			return mensagemDesenvolvedor;
		}
		
		
	}
	
	

}
