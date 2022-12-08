package com.lucas.minhasfinancas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucas.minhasfinancas.exception.RegraDeNegocioException;
import com.lucas.minhasfinancas.model.entity.Lancamento;
import com.lucas.minhasfinancas.model.enums.StatusLancamento;
import com.lucas.minhasfinancas.model.enums.TipoLancamento;
import com.lucas.minhasfinancas.model.repository.LancamentoRepository;
import com.lucas.minhasfinancas.service.LancamentoService;

@Service
public class LancamentoServiceImpl implements LancamentoService {

    private LancamentoRepository repository;

    @Autowired
    public LancamentoServiceImpl(LancamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Lancamento salvar(Lancamento lancamento) {
        validarLancamento(lancamento);
        lancamento.setStatus(StatusLancamento.PENDENTE);
        return repository.save(lancamento);
    }

    @Override
    @Transactional
    public Lancamento atualizar(Lancamento lancamento) {
        Objects.requireNonNull(lancamento.getId());
        validarLancamento(lancamento);
        return repository.save(lancamento);
    }

    @Override
    @Transactional
    public void deletar(Lancamento lancamento) {
        Objects.nonNull(lancamento.getId());
        repository.deleteById(lancamento.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
        Example<Lancamento> example = Example.of(lancamentoFiltro,
                ExampleMatcher.matching().withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING));

        return repository.findAll(example);
    }

    @Override
    public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
        lancamento.setStatus(status);
        atualizar(lancamento);
    }

    @Override
    public void validarLancamento(Lancamento lancamento) {

        if (lancamento.getDescricao() == null || lancamento.getDescricao().trim().isEmpty()) {
            throw new RegraDeNegocioException("Informe uma Descrição válida.");

        }

        if (lancamento.getMes() == null || lancamento.getMes() > 12) {
            throw new RegraDeNegocioException("Informe um Mês válido.");
        }

        if (lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
            throw new RegraDeNegocioException("Informe um Ano válido.");
        }

        if (lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
            throw new RegraDeNegocioException("Informe um Usuário");
        }

        if (lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
            throw new RegraDeNegocioException("Informe um Valor válido.");
        }

        if (lancamento.getTipo() == null) {
            throw new RegraDeNegocioException("Informe um Tipo de Lançamento.");
        }
    }

    @Override
    public Optional<Lancamento> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obterSaldoPorUsuario(Long idUsuario) {
        BigDecimal receitas = repository.obterSaldoPorTipoDeLancamentoEUsuario(idUsuario,
                TipoLancamento.RECEITA);

        BigDecimal despesas = repository.obterSaldoPorTipoDeLancamentoEUsuario(idUsuario,
                TipoLancamento.DESPESA);

        if (receitas == null) {
            receitas = BigDecimal.ZERO;

        } else if (despesas == null) {
            despesas = BigDecimal.ZERO;
        }

        return receitas.subtract(despesas);
    }

}
