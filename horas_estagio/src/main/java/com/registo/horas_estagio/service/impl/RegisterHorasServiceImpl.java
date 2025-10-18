package com.registo.horas_estagio.service.impl;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.PageResponse;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import com.registo.horas_estagio.mapper.RequestMapper;
import com.registo.horas_estagio.models.RegisterHoras;
import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.repository.RegistroHorasRepository;
import com.registo.horas_estagio.repository.UsuarioRepository;
import com.registo.horas_estagio.service.RegisterHorasService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterHorasServiceImpl implements RegisterHorasService {
    private static final Logger log = LoggerFactory.getLogger(RegisterHorasServiceImpl.class);

    private final RequestMapper requestMapper;
    private final RegistroHorasRepository registroHorasRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public RegisterResponse submitHours(RegisterRequest request) {
        log.info("Criando novo registro de horas para o estagiário: {}", request.estagiario());

        RegisterHoras registerHoras = requestMapper.mapToRegisterHoras(request);
        // Busca e associa o usuário
        Usuario usuario = usuarioRepository.findByUsername(request.estagiario())
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado: {}", request.estagiario());
                    return new RuntimeException("Usuário não encontrado: " + request.estagiario());
                });
        registerHoras.setUsuario(usuario);

        // Calcula horas automaticamente se não fornecido
        if (shouldCalculateHours(request)) {
            int horasCalculadas = calculateHoursBetween(request.dataInicio(), request.dataFim());
            registerHoras.setHorasTrabalhadas(horasCalculadas);
            log.debug("Horas calculadas automaticamente: {} horas", horasCalculadas);
        }

        RegisterHoras saved = registroHorasRepository.save(registerHoras);
        log.info("Registro criado com sucesso. ID: {}", saved.getId());
        return requestMapper.mapRegisterResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegisterResponse> findAllRegisteredHours() {
        log.debug("Buscando todos os registros de horas");
        List<RegisterHoras> registros = registroHorasRepository.findAll();
        log.info("Encontrados {} registros", registros.size());
        return requestMapper.mapToListRegisterResponse(registros);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RegisterResponse> findAllRegisteredHours(Pageable pageable) {
        log.debug("Buscando registros paginados - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<RegisterHoras> page = registroHorasRepository.findAll(pageable);
        List<RegisterResponse> content = requestMapper.mapToListRegisterResponse(page.getContent());

        log.info("Encontrados {} registros na página {} de {}",
                page.getNumberOfElements(), page.getNumber(), page.getTotalPages());

        return PageResponse.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegisterResponse> findAllRegisteredHoursUser(String name) {
        log.debug("Buscando registros para o usuário: {}", name);

        List<RegisterHoras> registos = registroHorasRepository.findByEstagiario(name);

        log.info("Encontrados {} registros para o usuário: {}", registos.size(), name);
        return requestMapper.mapToListRegisterResponse(registos);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RegisterResponse> findAllRegisteredHoursUser(String name, Pageable pageable) {
        log.debug("Buscando registros paginados do usuário: {} - Página: {}, Tamanho: {}",
                name, pageable.getPageNumber(), pageable.getPageSize());

        Page<RegisterHoras> page = registroHorasRepository.findByEstagiario(name, pageable);
        List<RegisterResponse> content = requestMapper.mapToListRegisterResponse(page.getContent());

        log.info("Encontrados {} registros para o usuário {} na página {} de {}",
                page.getNumberOfElements(), name, page.getNumber(), page.getTotalPages());

        return PageResponse.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }


    @Override
    @Transactional
    public void DeleteRegisteredHoursUser(UUID uuid) {
        log.info("Deletando registro com ID: {}", uuid);

        RegisterHoras registerHoras = registroHorasRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.error("Registro não encontrado para deletar. ID: {}", uuid);
                    return new RuntimeException("Registro não encontrado com ID: " + uuid);
                });

        registroHorasRepository.delete(registerHoras);
        log.info("Registro {} deletado com sucesso", uuid);
    }

    @Override
    @Transactional
    public RegisterResponse updateRegister(UUID uuid, RegisterRequest request) {
        log.info("Atualizando registro com ID: {}", uuid);

        // 1. Buscar registro existente
        RegisterHoras registerHoras = registroHorasRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.error("Registro não encontrado com ID: {}", uuid);
                    return new RuntimeException("Registro não encontrado com ID: " + uuid);
                });

        // 2. Atualizar campos básicos
        String estagiarioAnterior = registerHoras.getEstagiario();
        updateBasicFields(registerHoras, request);

        // 3. Atualizar usuário se o estagiário mudou
        if (!estagiarioAnterior.equals(request.estagiario())) {
            log.info("Alterando estagiário de '{}' para '{}'", estagiarioAnterior, request.estagiario());
            updateEstagiario(registerHoras, request.estagiario());
        }

        // 4. Recalcular horas se necessário
        if (shouldCalculateHours(request)) {
            int horasCalculadas = calculateHoursBetween(request.dataInicio(), request.dataFim());
            registerHoras.setHorasTrabalhadas(horasCalculadas);
            log.debug("Horas recalculadas: {}", horasCalculadas);
        } else {
            registerHoras.setHorasTrabalhadas(request.horasTrabalhadas());
        }

        // 5. Salvar alterações
        RegisterHoras updated = registroHorasRepository.save(registerHoras);
        log.info("Registro {} atualizado com sucesso", uuid);

        return requestMapper.mapRegisterResponse(updated);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void updateEstagiario(RegisterHoras registerHoras, String novoEstagiario) {
        Usuario usuario = usuarioRepository.findByUsername(novoEstagiario)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado: {}", novoEstagiario);
                    return new RuntimeException("Usuário não encontrado: " + novoEstagiario);
                });

        registerHoras.setEstagiario(novoEstagiario);
        registerHoras.setUsuario(usuario);
        log.debug("Estagiário atualizado para: {}", novoEstagiario);
    }

    private void updateBasicFields(RegisterHoras registerHoras, RegisterRequest request) {
        registerHoras.setDescricao(request.descricao());
        registerHoras.setDataInicio(request.dataInicio());
        registerHoras.setDataFim(request.dataFim());
        registerHoras.setHorasTrabalhadas(request.horasTrabalhadas());
        log.debug("Campos básicos atualizados");
    }

    /**
     * Verifica se deve calcular as horas automaticamente
     */
    private boolean shouldCalculateHours(RegisterRequest request) {
        return request.horasTrabalhadas() <= 0
                && Objects.nonNull(request.dataInicio())
                && Objects.nonNull(request.dataFim());
    }

    /**
     * Calcula as horas trabalhadas entre duas datas
     */
    private Integer calculateHoursBetween(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataFim.isBefore(dataInicio)) {
            log.error("Data fim ({}) é anterior à data início ({})", dataFim, dataInicio);
            throw new RuntimeException("Data fim não pode ser anterior à data início");
        }

        Duration duration = Duration.between(dataInicio, dataFim);
        long horas = duration.toHours();

        if (horas > 24) {
            log.warn("Horas calculadas excedem 24 horas: {} horas", horas);
        }

        return Math.toIntExact(horas);
    }
}
