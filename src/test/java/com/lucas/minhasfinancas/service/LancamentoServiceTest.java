package com.lucas.minhasfinancas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lucas.minhasfinancas.exception.RegraDeNegocioException;
import com.lucas.minhasfinancas.model.entity.Lancamento;
import com.lucas.minhasfinancas.model.entity.Usuario;
import com.lucas.minhasfinancas.model.enums.StatusLancamento;
import com.lucas.minhasfinancas.model.repository.LancamentoRepository;
import com.lucas.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.lucas.minhasfinancas.service.impl.LancamentoServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

    @SpyBean
    LancamentoServiceImpl service;

    @MockBean
    LancamentoRepository repository;

    @Test
    public void deveSalvarUmLancamento() {
        // Cenário
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
        Mockito.doNothing().when(service).validarLancamento(lancamentoASalvar);

        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1L);
        lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);

        Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);

        // Execução
        Lancamento lancamento = service.salvar(lancamentoASalvar);

        // Verificação
        assertEquals(lancamento.getId(), lancamentoSalvo.getId());
        assertEquals(lancamento.getStatus(), StatusLancamento.PENDENTE);
    }

    @Test
    public void naoDeveSalvarUmLancamentoQuandoHouverErroValidacao() {
        // Cenário
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
        Mockito.doThrow(RegraDeNegocioException.class).when(service).validarLancamento(lancamentoASalvar);

        // Execução
        assertThrows(RegraDeNegocioException.class, () -> service.salvar(lancamentoASalvar));

        // Verificação
        Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
    }

    @Test
    public void deveAtualizarUmLancamento() {
        // Cenário
        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1L);
        lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
        Mockito.doNothing().when(service).validarLancamento(lancamentoSalvo);

        Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);

        // Execução
        service.atualizar(lancamentoSalvo);

        // Verificação
        Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
    }

    @Test
    public void naoDeveAtualizarUmLancamentoSemId() {
        // Cenário
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();

        // Execução e Verificação
        assertThrows(NullPointerException.class, () -> service.atualizar(lancamentoASalvar));
        Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
    }

    @Test
    public void deveDeletarUmLancamento() {
        // Cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1L);

        // Execução e Verificação
        service.deletar(lancamento);

        Mockito.verify(repository).deleteById(lancamento.getId());
    }

    @Test
    public void naoDeveDeletarUmLancamentoSemId() {
        // Cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        // Execução e Verificação
        service.deletar(lancamento);

        Mockito.verify(repository, Mockito.never()).delete(lancamento);
    }

    @Test
    public void deveBuscarLancamento() {
        // Cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1L);

        List<Lancamento> lancamentos = Arrays.asList(lancamento);
        Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lancamentos);

        // Execucao
        List<Lancamento> resultado = service.buscar(lancamento);

        // Verificacao
        Assertions.assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
    }

    @Test
    public void deveAtualizarStatusDoLancamento() {
        // Cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1L);

        StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
        Mockito.doReturn(lancamento).when(service).atualizar(lancamento);

        // Execucao
        service.atualizarStatus(lancamento, novoStatus);

        // verificacao
        assertEquals(lancamento.getStatus(), novoStatus);
        Mockito.verify(service).atualizar(lancamento);
    }

    @Test
    public void deveObterLancamentoPorId() {
        // Cenário
        Long id = 1L;

        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));

        // Execucao
        Optional<Lancamento> lancamentoBuscado = service.buscarPorId(id);

        // Verificacao
        Assertions.assertThat(lancamentoBuscado.isPresent()).isTrue();
    }

    @Test
    public void deveRetornarVazioSeLancamentoNaoExiste() {
        // Cenário
        Long id = 1L;

        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        // Execucao
        Optional<Lancamento> lancamentoBuscado = service.buscarPorId(id);

        // Verificacao
        Assertions.assertThat(lancamentoBuscado.isPresent()).isFalse();
    }

    @Test
    public void deveLancarExcecaoAoValidarCampos() {
        // Cenário
        Lancamento lancamento = new Lancamento();

        // Descrição null
        Throwable erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe uma Descrição válida.");

        // Descrição vazia.
        lancamento.setDescricao("");

        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe uma Descrição válida.");

        lancamento.setDescricao("Salário");

        // Mes null
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Mês válido.");

        lancamento.setMes(0);

        // Mes < 1
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Mês válido.");

        lancamento.setMes(13);

        // Mes > 12
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Mês válido.");

        lancamento.setMes(1);

        // Ano null
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Ano válido.");

        // Ano < 4
        lancamento.setAno(202);

        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Ano válido.");

        lancamento.setAno(2020);

        // Usuário null
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Usuário");

        lancamento.setUsuario(new Usuario());

        // Id do usuario null
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Usuário");

        lancamento.setUsuario(Usuario.builder().id(1L).nome("usuario").email("usuario@gmail.com").build());

        // Valor null
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Valor válido.");

        lancamento.setValor(BigDecimal.ZERO);

        // Valor = 0
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Valor válido.");

        lancamento.setValor(BigDecimal.valueOf(150));

        // Tipo de lancamento null
        erro = Assertions.catchThrowable(() -> service.validarLancamento(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Informe um Tipo de Lançamento.");
    }
}
