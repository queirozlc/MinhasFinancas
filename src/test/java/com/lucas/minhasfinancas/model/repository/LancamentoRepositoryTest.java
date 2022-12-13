package com.lucas.minhasfinancas.model.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lucas.minhasfinancas.model.entity.Lancamento;
import com.lucas.minhasfinancas.model.enums.StatusLancamento;
import com.lucas.minhasfinancas.model.enums.TipoLancamento;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class LancamentoRepositoryTest {

    @Autowired
    private LancamentoRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    public void deveSalvarUmLancamento() {
        Lancamento lancamento = criarLancamento();

        lancamento = repository.save(lancamento);

        assertNotNull(lancamento.getId());
    }

    @Test
    public void deveDeletarUmLancamento() {
        Lancamento lancamento = criarPersistirLancamento();

        lancamento = em.find(Lancamento.class, lancamento.getId());

        repository.delete(lancamento);

        Lancamento lancamentoInexistente = em.find(Lancamento.class, lancamento.getId());

        assertNull(lancamentoInexistente);
    }

    @Test
    public void deveAtualizarLancamento() {
        Lancamento lancamento = criarPersistirLancamento();

        lancamento.setAno(2017);
        lancamento.setDescricao("Teste Update");
        lancamento.setStatus(StatusLancamento.CANCELADO);

        repository.save(lancamento);

        Lancamento lancamentoAtualizado = em.find(Lancamento.class, lancamento.getId());

        assertEquals(lancamentoAtualizado.getAno(), 2017);
        assertEquals(lancamentoAtualizado.getDescricao(), "Teste Update");
        assertEquals(lancamentoAtualizado.getStatus(), StatusLancamento.CANCELADO);
    }

    @Test
    public void deveEncontrarLancamentoPorId() {
        Lancamento lancamento = criarPersistirLancamento();

        Optional<Lancamento> lancamentoEncontrado = repository.findById(lancamento.getId());

        assertTrue(lancamentoEncontrado.isPresent());
    }

    private Lancamento criarPersistirLancamento() {
        Lancamento lancamento = criarLancamento();
        em.persist(lancamento);
        return lancamento;
    }

    public static Lancamento criarLancamento() {
        return Lancamento.builder().ano(2022).mes(6).descricao("Salário").valor(BigDecimal.valueOf(20))
                .tipo(TipoLancamento.RECEITA).status(StatusLancamento.PENDENTE).dataCadastro(LocalDate.now()).build();
    }

}
