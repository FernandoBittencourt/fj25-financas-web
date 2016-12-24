package br.com.caelum.financas.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.com.caelum.financas.modelo.Conta;

@Stateless
public class ContaDao {
	
	@Inject
	EntityManager manager;

	public void adiciona(Conta conta) {
		this.manager.joinTransaction();
		this.manager.persist(conta);
	}
	
	public void altera(Conta conta) {
		this.manager.joinTransaction();
		this.manager.merge(conta);
	}

	public Conta busca(Integer id) {
		this.manager.joinTransaction();
		return this.manager.find(Conta.class, id);
	}

	public List<Conta> lista() {
		this.manager.joinTransaction();
		return this.manager.createQuery("select c from Conta c", Conta.class).getResultList();
	}

	public void remove(Conta conta) {
		this.manager.joinTransaction();
		Conta contaParaRemover = this.manager.find(Conta.class, conta.getId());
		this.manager.remove(contaParaRemover);
	}

}




