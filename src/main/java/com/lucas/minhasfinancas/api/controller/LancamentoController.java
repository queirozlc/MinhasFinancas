package com.lucas.minhasfinancas.api.controller;

import com.lucas.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.lucas.minhasfinancas.api.dto.LancamentoDTO;
import com.lucas.minhasfinancas.exception.RegraDeNegocioException;
import com.lucas.minhasfinancas.model.entity.Lancamento;
import com.lucas.minhasfinancas.model.entity.Usuario;
import com.lucas.minhasfinancas.model.enums.StatusLancamento;
import com.lucas.minhasfinancas.model.enums.TipoLancamento;
import com.lucas.minhasfinancas.service.LancamentoService;
import com.lucas.minhasfinancas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoController {

    private final LancamentoService service;
    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable(name = "id") Long id) {
        return service.buscarPorId(id)
                .map(lancamento -> new ResponseEntity<>(converterParaDto(lancamento), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/salvarlancamento")
    public ResponseEntity<?> salvarLancamento(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento lancamento = this.converter(dto);
            lancamento = service.salvar(lancamento);
            return new ResponseEntity<Lancamento>(lancamento, HttpStatus.CREATED);

        } catch (RegraDeNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> buscarLancamento(@RequestParam(value = "descricao", required = false) String descricao,
                                              @RequestParam(value = "mes", required = false) Integer mes,
                                              @RequestParam(value = "ano", required = false) Integer ano,
                                              @RequestParam(value = "usuario") Long idUsuario,
                                              @RequestParam(value = "tipo", required = false) TipoLancamento tipo) {

        Optional<Usuario> usuario = usuarioService.buscarPorId(idUsuario);

        Lancamento lancamentoFiltro = new Lancamento();
        lancamentoFiltro.setDescricao(descricao);
        lancamentoFiltro.setMes(mes);
        lancamentoFiltro.setAno(ano);
        lancamentoFiltro.setTipo(tipo);

        if (!usuario.isPresent()) {
            return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuário não encontrado.");

        } else {
            lancamentoFiltro.setUsuario(usuario.get());
        }

        List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);

        return new ResponseEntity<List<Lancamento>>(lancamentos, HttpStatus.OK);
    }

    @PutMapping("/atualizarlancamento/{id}")
    public ResponseEntity<?> atualizarLancamento(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {

        return service.buscarPorId(id).map(entity -> {
            try {
                Lancamento lancamento = converter(dto);
                lancamento.setId(entity.getId());
                service.atualizar(lancamento);
                return ResponseEntity.ok(lancamento);

            } catch (RegraDeNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity<>("Lançamento não encontrado na base de dados.",
                HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id) {

        return service.buscarPorId(id).map(entity -> {
            service.deletar(entity);
            return new ResponseEntity<Lancamento>(HttpStatus.NO_CONTENT);
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de dados.",
                HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/atualizarstatus/{id}")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody AtualizaStatusDTO dto) {

        return service.buscarPorId(id).map(entity -> {

            StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());

            if (statusSelecionado == null) {
                return ResponseEntity.badRequest()
                        .body("Não foi possível atualizar o status do lançamento.Status inválido.");
            }

            try {
                entity.setStatus(statusSelecionado);
                service.atualizar(entity);
                return ResponseEntity.ok(entity);
            } catch (RegraDeNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

        }).orElseGet(() -> new ResponseEntity<>("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));
    }

    private Lancamento converter(LancamentoDTO dto) {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId());
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setMes(dto.getMes());
        lancamento.setAno(dto.getAno());
        lancamento.setValor(dto.getValor());

        Usuario usuario = usuarioService
                .buscarPorId(dto.getUsuario())
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado com Id informado."));

        lancamento.setUsuario(usuario);

        if (dto.getTipo() != null) {
            lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
        }

        if (dto.getStatus() != null) {
            lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
        }

        return lancamento;
    }

    private LancamentoDTO converterParaDto(Lancamento lancamento) {
        return LancamentoDTO.builder()
                .id(lancamento.getId())
                .descricao(lancamento.getDescricao())
                .valor(lancamento.getValor())
                .mes(lancamento.getMes())
                .ano(lancamento.getAno())
                .status(lancamento.getStatus().name())
                .tipo(lancamento.getTipo().name())
                .usuario(lancamento.getUsuario().getId())
                .build();
    }
}
