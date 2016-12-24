package br.com.caelum.financas.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import br.com.caelum.financas.modelo.Conta;

public class NumeroEAgenciaValidator implements ConstraintValidator<NumeroEAgencia, Conta>{
	public void initialize(NumeroEAgencia anotacao){		
	}
	
	public boolean isValid(Conta conta, ConstraintValidatorContext ctx){
		if(conta==null){
			return true;
		}
		return !(conta.getAgencia() == null ^ conta.getNumero() == null);
	}
}
