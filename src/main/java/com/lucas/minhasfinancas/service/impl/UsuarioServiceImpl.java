package com.lucas.minhasfinancas.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return null;
    }

    @Override
    public Usuario salvarUsuario(Usuario usuario) {

        return null;
    }

    @Override
    public void validarEmail(String email) {
        boolean existeEmail = repository.existsByEmail(email);

        if (existeEmail) {
            throw new RegraDeNegocioException("Já existe um usuário cadastrado com esse email.");
        }
    }

}
