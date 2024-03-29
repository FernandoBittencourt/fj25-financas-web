package br.com.caelum.financas.dao;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.com.caelum.financas.exception.ValorInvalidoException;
import br.com.caelum.financas.modelo.Conta;
import br.com.caelum.financas.modelo.Movimentacao;
import br.com.caelum.financas.modelo.TipoMovimentacao;
import br.com.caelum.financas.modelo.ValorPorMesEAno;

@Stateless
public class MovimentacaoDao {

	@Inject
	EntityManager manager;

	public void adiciona(Movimentacao movimentacao) {
		this.manager.joinTransaction();
		this.manager.persist(movimentacao);
		if(movimentacao.getValor().compareTo(BigDecimal.ZERO)<0){
			throw new ValorInvalidoException("Movimentacao negativa");
		}
	}

	public Movimentacao busca(Integer id) {
		this.manager.joinTransaction();
		return this.manager.find(Movimentacao.class, id);
	}

	public List<Movimentacao> lista() {
		this.manager.joinTransaction();
		return this.manager.createQuery("select m from Movimentacao m", Movimentacao.class).getResultList();
	}

	public void remove(Movimentacao movimentacao) {
		this.manager.joinTransaction();
		Movimentacao movimentacaoParaRemover = this.manager.find(Movimentacao.class, movimentacao.getId());
		this.manager.remove(movimentacaoParaRemover);
	}
	
	public List<Movimentacao> listaTodasMovimentacoes(Conta conta){
		this.manager.joinTransaction();
		String jpql = "select m from Movimentacao m "
				+ "where m.conta = :conta order by m.valor desc";
		Query query = this.manager.createQuery(jpql);
		query.setParameter("conta", conta);
		return query.getResultList();
	}
	
	public List<Movimentacao> listaPorValorETipo(BigDecimal valor, TipoMovimentacao tipo){
		this.manager.joinTransaction();
		String jpql = "select m from Movimentacao m where m.valor <= :valor and m.tipoMovimentacao = :tipo";
		TypedQuery<Movimentacao> query = this.manager.createQuery(jpql,Movimentacao.class);
		query.setParameter("valor", valor)
			.setParameter("tipo", tipo);
		return query.getResultList();
	}
	
	public BigDecimal calculaTotalMovimentacao(Conta conta, TipoMovimentacao tipo){
		this.manager.joinTransaction();
		String jpql = "select sum(m.valor) from Movimentacao m where m.conta = :conta and m.tipoMovimentacao = :tipo";
		TypedQuery<BigDecimal> query = this.manager.createQuery(jpql,BigDecimal.class);
		query.setParameter("conta", conta)
		.setParameter("tipo", tipo);
		
		return query.getSingleResult();
	}
	
	public List<Movimentacao> buscaTodasMovimentacoesDaConta(String titular){
		this.manager.joinTransaction();
		String jpql = "select m from Movimentacao m where m.conta.titular like :titular";
		TypedQuery<Movimentacao> query = this.manager.createQuery(jpql,Movimentacao.class);
		
		query.setParameter("titular", "%"+titular+"%");
		
		return query.getResultList();
	}

	public List<ValorPorMesEAno> listaMesesComMovimentacoes(Conta conta, TipoMovimentacao tipo){
		this.manager.joinTransaction();
		String jpql = "select new br.com.caelum.financas.modelo.ValorPorMesEAno(month(m.data),"
				+ "year(m.data), sum(m.valor)) from Movimentacao m "
				+ "where m.conta = :conta and m.tipoMovimentacao = :tipo "
				+ "group by year(m.data) || month(m.data) "
				+ "order by sum(valor) desc";
		
		TypedQuery<ValorPorMesEAno> query = this.manager.createQuery(jpql, ValorPorMesEAno.class);
		
		query.setParameter("conta", conta)
			.setParameter("tipo", tipo);
		
		return query.getResultList();
	}
	
	public List<Movimentacao> listaComCategorias(){
		this.manager.joinTransaction();
		String jpql = "select distinct m from Movimentacao m left join fetch m.categorias";
		
		TypedQuery<Movimentacao> query = this.manager.createQuery(jpql, Movimentacao.class);		
		return query.getResultList();
	}
	
	public List<Movimentacao> listaTodasComCriteria(){
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Movimentacao> criteria = builder.createQuery(Movimentacao.class);
		criteria.from(Movimentacao.class);
		return this.manager.createQuery(criteria).getResultList();
	}
	
	public BigDecimal somaMovimentacoesDoTitular(String titular) {
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<BigDecimal> criteria = builder.createQuery(BigDecimal.class);
		
		Root<Movimentacao> root = criteria.from(Movimentacao.class);
		criteria.select(builder.sum(root.<BigDecimal>get("valor")));
		criteria.where(builder.like(root.<Conta>get("conta").<String>get("titular"), "%"+titular+"%"));
		return this.manager.createQuery(criteria).getSingleResult();
	}
	
	public List<Movimentacao> pesquisa(Conta conta, TipoMovimentacao tipoMovimentacao, Integer mes) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Movimentacao> criteria = builder.createQuery(Movimentacao.class);
		
		Root<Movimentacao> root = criteria.from(Movimentacao.class);
		
		Predicate conjunction = builder.conjunction();
		if(conta.getId() != null){
			conjunction = builder.and(conjunction, builder.equal(root.<Conta> get("conta"),conta));
		}
		
		if(mes != null && mes!= 0){
			Expression<Integer> expression = builder.function("month", Integer.class, root.<Calendar> get("data"));
			conjunction = builder.and(conjunction, builder.equal(expression, mes));
		}
		
		if(tipoMovimentacao!=null){
			conjunction = builder.and(conjunction,builder.equal(root.<TipoMovimentacao>get("tipoMovimentacao"), tipoMovimentacao));
		}
		
		criteria.where(conjunction);
		
		return manager.createQuery(criteria).getResultList();		
	}
}
