package com.lucas.minhasfinancas.model.repository;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lucas.minhasfinancas.model.entity.Usuario;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UsuarioRepositoryTest {

    @Autowired
    UsuarioRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    public void deveVerificarExistenciaEmail() {
        // Cenário
        Usuario usuario = criarUsuario();
        entityManager.persist(usuario);

        // Ação
        boolean resultado = repository.existsByEmail(usuario.getEmail());
        ;

        // Verificação
        Assertions.assertTrue(resultado);
    }

    @Test
    public void retornaFalsoQuandoNaoTemCadastroEmail() {
        // Cenario

        // Ação
        boolean resultado = repository.existsByEmail("usuario@gmail.com");

        // verificação
        Assertions.assertFalse(resultado);

    }

    @Test
    public void devePersistirUsuarioNaBaseDeDados() {
        // Cenario
        Usuario usuario = criarUsuario();

        // Ação
        Usuario usuarioSalvo = repository.save(usuario);

        // Verificação
        Assertions.assertNotNull(usuarioSalvo.getId());
    }

    @Test
    public void deveBuscarUsuarioPorEmail() {
        // Cenario
        Usuario usuario = criarUsuario();
        entityManager.persist(usuario);

        // Verificação
        Optional<Usuario> modelo = repository.findByEmail(usuario.getEmail());

        Assertions.assertTrue(modelo.isPresent());
    }

    public static Usuario criarUsuario() {
        return Usuario.builder().nome("usuario").email("usuario@gmail.com").senha("senha").build();
    }

}
