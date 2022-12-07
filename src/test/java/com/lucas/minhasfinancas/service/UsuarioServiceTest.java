package com.lucas.minhasfinancas.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lucas.minhasfinancas.exception.ErroAutenticacao;
import com.lucas.minhasfinancas.exception.RegraDeNegocioException;
import com.lucas.minhasfinancas.model.entity.Usuario;
import com.lucas.minhasfinancas.model.repository.UsuarioRepository;
import com.lucas.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @SpyBean
    UsuarioServiceImpl service;

    @MockBean
    UsuarioRepository repository;

    @Test()
    public void deveValidarEmail() {
        Assertions.assertDoesNotThrow(() -> {
            // Cenário
            Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

            // Ação
            service.validarEmail("email@gmail.com");
        });
    }

    @Test
    public void lancaErroAoValidarEmailQuandoExisteEmail() {
        Assertions.assertThrows(RegraDeNegocioException.class, () -> {

            // Cenario
            Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

            // Ação
            service.validarEmail("usuario@gmail.com");

        });
    }

    // Possibilidade 1 de autenticação do usuário
    @Test
    public void deveAutenticarUsuarioComSucesso() {
        Assertions.assertDoesNotThrow(() -> {
            // Cenario
            String email = "usuario@gmail.com";
            String senha = "usuario";

            Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
            Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

            // Ação
            service.autenticar(email, senha);

            // Verificação
            Assertions.assertNotNull(usuario);
        });
    }

    // Possibilidade 2 de autenticação do usuário
    @Test
    public void deveRetornarExcecaoAoNaoBuscarUsuarioComEmail() {
        // Cenario
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        // Ação
        Throwable exception = Assertions.assertThrows(ErroAutenticacao.class, () -> {
            service.autenticar("usuario@gmail.com", "senha");
        });

        // Verificação
        Assertions.assertEquals("Usuário não foi encontrado.", exception.getMessage());
    }

    // Possibilidade 3 de autenticação do usuário
    @Test
    public void deveLancarExcecaoQuandoSenhaNaoBater() {
        // Cenário
        Usuario usuario = Usuario.builder().senha("senha").email("usuario@gmail.com").build();
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

        // Ação

        Throwable exception = Assertions.assertThrows(ErroAutenticacao.class, () -> {
            service.autenticar("usuario@gmail.com", "outrasenha");
        });

        // Verificação
        Assertions.assertEquals("Senha incorreta.", exception.getMessage());
    }

    // Possibilidade 1 do cadastro de um usuário
    @Test
    public void deveCadastrarUsuario() {
        Assertions.assertDoesNotThrow(() -> {
            // Cenário
            Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
            Usuario usuario = Usuario.builder().id(1L).nome("usuario").email("usuario@gmail.com").senha("senha")
                    .build();

            Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

            // Ação
            Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

            // Verificação
            Assertions.assertNotNull(usuarioSalvo);
            Assertions.assertEquals(1L, usuarioSalvo.getId());
            Assertions.assertEquals(usuarioSalvo.getNome(), "usuario");
            Assertions.assertEquals(usuarioSalvo.getEmail(), "usuario@gmail.com");
            Assertions.assertEquals(usuarioSalvo.getSenha(), "senha");
        });

    }

    // Possibilidade 2 do cadastro de um usuário
    @Test
    public void naoDeveSalvarUsuarioComEmailJaCadastrado() {
        Assertions.assertThrows(RegraDeNegocioException.class, () -> {
            // Cenário
            Usuario usuario = Usuario.builder().email("usuario@gmail.com").build();
            Mockito.doThrow(RegraDeNegocioException.class).when(service).validarEmail(usuario.getEmail());

            // Ação
            service.salvarUsuario(usuario);

            // Verificação
            Mockito.verify(repository, Mockito.never()).save(usuario);
        });
    }
}
