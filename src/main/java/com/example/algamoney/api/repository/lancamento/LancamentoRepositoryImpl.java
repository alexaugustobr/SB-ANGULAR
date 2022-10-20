package com.example.algamoney.api.repository.lancamento;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.util.ObjectUtils;
import org.springframework.data.domain.Pageable;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Lancamento_;
import com.example.algamoney.api.repository.filter.LancamentoFilter;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	
	// Isto é para trabalhar com a consulta
	@PersistenceContext
	private EntityManager manager;
	
	
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		//Criteria API é uma interface do JPA que permite a criação de querys programaticamente. Isso dá a possibilidade
		//de criar querys dinâmicas a partir dos parâmetros passados, evitando o trabalho -arriscado- de se criar strings
		//de buscas através de concatenação.
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		// A instância do CriteriaQuery é usada para criar um objeto de consulta	
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		
		// Aqui é a parte responsável pelos Wheres da consulta.
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		// criar as restrições 
		// Predicate<T> é uma interface com uma função que recebe um argumento e retorna um boolean, que geralmente
		//é usado para escrever lambdas de filtragem.
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		// Usada para controlar a execução de consultas digitadas.
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		// Aqui é retonardo o contéudo, o pageable e ototal que se quer retornar.
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter)) ;
	}


	








	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		
		List<Predicate> predicates = new ArrayList<>();
		
		// where descricao like '%aasasasaasaas%'
		// É o mesmo que debaixo
		// objectutils possui os mais diversos utilitários para trabalhar 
		// O uso do metamodel é para evitar escrever strings e não errar.
		if(!ObjectUtils.isEmpty(lancamentoFilter.getDescricao())) {
		    predicates.add(builder.like(
		            builder.lower(root.get(Lancamento_.descricao)), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		}
		

		if (lancamentoFilter.getDataVencimentoDe() !=null) {
			predicates.add(
					builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoDe()));
		}

		if (lancamentoFilter.getDataVencimentoAte() !=null) {
			predicates.add(
					builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoAte()));
		}
		
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}

	
	private void adicionarRestricoesDePaginacao(TypedQuery<Lancamento> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;

		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
	}
	

	private Long total(LancamentoFilter lancamentoFilter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);

		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
        // Aqui abaixo é contado e retornado o resultado da coonsulta
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}






	
}
