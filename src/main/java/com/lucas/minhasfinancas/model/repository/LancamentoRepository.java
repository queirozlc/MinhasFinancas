package com.lucas.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucas.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
