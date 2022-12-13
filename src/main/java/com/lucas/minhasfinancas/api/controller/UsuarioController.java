package com.lucas.minhasfinancas.api.controller;

import java.math.BigDecimal;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.minhasfinancas.api.dto.UsuarioDTO;
import com.lucas.minhasfinancas.exception.ErroAutenticacao;
import com.lucas.minhasfinancas.exception.RegraDeNegocioException;
import com.lucas.minhasfinancas.model.entity.Usuario;
import com.lucas.minhasfinancas.service.LancamentoService;
import com.lucas.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;
    private final LancamentoService lancamentoService;

    @PostMapping("/salvarusuario")
    @Transactional
    public ResponseEntity<?> salvarUsuario(@RequestBody UsuarioDTO dto) {

        Usuario usuario = Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).senha(dto.getSenha()).build();

        try {
            Usuario usuarioSalvo = service.salvarUsuario(usuario);

            return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.CREATED);
        } catch (RegraDeNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticarUsuario(@RequestBody UsuarioDTO dto) {

        try {
            Usuario usuarioAutenticado = service.autenticar(dto.getEmail(), dto.getSenha());

            return new ResponseEntity<Usuario>(usuarioAutenticado, HttpStatus.OK);
        } catch (ErroAutenticacao e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/consultarsaldo/{id}")
    public ResponseEntity<?> consultarSaldo(@PathVariable Long id) {
        Optional<Usuario> usuario = service.buscarPorId(id);

        if (!usuario.isPresent()) {
            return new ResponseEntity<Usuario>(HttpStatus.NOT_FOUND);
        }

        BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);

        return ResponseEntity.ok(saldo);
    }
}
