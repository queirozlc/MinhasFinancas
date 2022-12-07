package com.lucas.minhasfinancas.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucas.minhasfinancas.exception.ErroAutenticacao;
import com.lucas.minhasfinancas.exception.RegraDeNegocioException;
import com.lucas.minhasfinancas.model.entity.Usuario;
import com.lucas.minhasfinancas.model.repository.UsuarioRepository;
import com.lucas.minhasfinancas.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private UsuarioRepository repository;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository repository) {
        super();
        this.repository = repository;
    }

    @Override
    public Usuario autenticar(String email, String senha) {
        Optional<Usuario> usuario = repository.findByEmail(email);

        if (!usuario.isPresent()) {
            throw new ErroAutenticacao("Usuário não foi encontrado.");
        }

        if (!usuario.get().getSenha().equals(senha)) {
            throw new ErroAutenticacao("Senha incorreta.");
        }

        return usuario.get();
    }

    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        validarEmail(usuario.getEmail());
        return repository.save(usuario);
    }

    @Override
    public void validarEmail(String email) {
        boolean existeEmail = repository.existsByEmail(email);

        if (existeEmail) {
            throw new RegraDeNegocioException("Já existe um usuário cadastrado com esse email.");
        }
    }

}
