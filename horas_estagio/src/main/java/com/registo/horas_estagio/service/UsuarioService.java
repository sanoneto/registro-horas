package com.registo.horas_estagio.service;

import com.registo.horas_estagio.models.Usuario;

import java.util.Optional;

public interface UsuarioService {

    public Usuario registrarUsuario(Usuario usuario) ;
    /**
     * Busca um usuário pelo username.
     * Exemplo: Usado em autenticação/login.
     */
    public Optional<Usuario> buscarPorUsername(String username) ;

    /**
     * Verifica se um usuário existe pelo nome de usuário.
     */
    public boolean existeUsuario(String username);
}
