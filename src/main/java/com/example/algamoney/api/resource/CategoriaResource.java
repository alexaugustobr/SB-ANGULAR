package com.example.algamoney.api.resource;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Categoria;
import com.example.algamoney.api.repository.CategoriaRepository;

@RestController
@RequestMapping("/categorias")
public class CategoriaResource {
	
	@Autowired
	private CategoriaRepository categoriaRepository;
	
	@Autowired
	// publicador de eventos de aplicação.
	private ApplicationEventPublisher publisher;
	
	// Essa anotação permite que todas as origens possam chamar o método listar.
	// no caso abaixo cadastramos a http://localhost:8000
	// maxAge = 10 é o tempo que vai demorar pra fazer uma nova requisição
	//@CrossOrigin (maxAge = 10, origins = {"http://localhost:8000"} )
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA') and hasAuthority('SCOPE_read')" )
	@GetMapping
	public List<Categoria> listar(){
		return categoriaRepository.findAll();
	}
	
	@PostMapping // Aqui abaixo vai permitir que quando criado uma categoria no json ela vai virar objeto
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_CATEGORIA') and hasAuthority('SCOPE_write')")
	// O servelet é um pequeno servidor que responsável por gerenciar requisições https se comunicando
	// com o cliente
	// O responseEntity retorna uma um objeto(categoria)
	// É necessário validar a categoria  para que as validações inseridas sejam reconhecidas e executadas
	// A aplicação do Spring Validation é bastante simples e prática e precisa ser reconhecida.
	public ResponseEntity<Categoria> criar(@Valid @RequestBody Categoria categoria, HttpServletResponse response){
		// Aqui é necessario instanciar o objeto categoriaSalva pois vamos precisar do id.
		
		Categoria categoriaSalva = categoriaRepository.save(categoria);
		
		publisher.publishEvent(new RecursoCriadoEvent(this, response, categoriaSalva.getCodigo()));
		// Abaixo pegamos a partir da requisição atual que foi pra /categorias adicionar o path código
		// e adicionar o código na URI setando o headerLocation com essa URI.
		
		
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
		
	
		
	}
	
	
	
	
	@GetMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA') and hasAuthority('SCOPE_read')" )
	// O @PathVariable é pq o id vai variar. 
	public ResponseEntity<Categoria> buscarPeloCodigo(@PathVariable Long codigo) {
	Optional<Categoria> categoria = this.categoriaRepository.findById(codigo);
	// operador ternário para se achar ok, do contrário retorna not found(404).
	return categoria.isPresent() ? 
	        ResponseEntity.ok(categoria.get()) : ResponseEntity.notFound().build();
	}
	

}


















