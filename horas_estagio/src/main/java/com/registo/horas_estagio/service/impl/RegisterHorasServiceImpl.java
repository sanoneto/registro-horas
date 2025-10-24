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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

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
        Usuario usuario = getUsuario(request.estagiario());
        registerHoras.setUsuario(usuario);

        // Calcula horas automaticamente se não fornecido
        if (shouldCalculateHours(request)) {
            double horasCalculadas = calculateHoursBetween(request.dataInicio(), request.dataFim());
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
    public void DeleteRegisteredHoursUser(UUID publicId) {
        log.info("Deletando registro com ID: {}", publicId);

        RegisterHoras registerHoras = registroHorasRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.error("Registro não encontrado para deletar. ID: {}", publicId);
                    return new RuntimeException("Registro não encontrado com ID: " + publicId);
                });

        registroHorasRepository.delete(registerHoras);
        log.info("Registro {} deletado com sucesso", publicId);
    }

    @Override
    @Transactional
    public RegisterResponse updateRegister(UUID publicId, RegisterRequest request) {
        log.info("Atualizando registro com ID: {}", publicId);

        // 1. Buscar registro existente
        RegisterHoras registerHoras = registroHorasRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.error("Registro não encontrado com ID: {}", publicId);
                    return new RuntimeException("Registro não encontrado com ID: " + publicId);
                });

        // 2. Atualizar campos básicos
        String estagiarioAnterior = registerHoras.getEstagiario();
        updateBasicFields(registerHoras, request);

        // 3. Atualizar usuário se o estagiário mudou
        if (!estagiarioAnterior.equals(request.estagiario())) {
            log.info("Alterando estagiário de '{}' para '{}'", estagiarioAnterior, request.estagiario());
            updateEstagiario(registerHoras, request.estagiario());
        }

        if (shouldCalculateHours(request)) {
            double horasCalculadas = calculateHoursBetween(request.dataInicio(), request.dataFim());
            // Salvar a representação decimal correta (ex.: 2.5 para 2h30m)
            registerHoras.setHorasTrabalhadas(horasCalculadas);
            log.debug("Horas recalculadas: {}", horasCalculadas);
        } else {
            registerHoras.setHorasTrabalhadas(request.horasTrabalhadas());
        }
        // 5. Salvar alterações
        RegisterHoras updated = registroHorasRepository.save(registerHoras);
        log.info("Registro {} atualizado com sucesso", publicId);

        return requestMapper.mapRegisterResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Double> getWeeklyHoursForYear(int year, String estagiario) {
        // Determina o número máximo de semanas no ano (52 ou 53)
        LocalDate dec28 = LocalDate.of(year, 12, 28); // garante semana ISO final do ano
        int maxWeek = dec28.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

        Map<Integer, Double> result = new HashMap<>();

        for (int week = 1; week <= maxWeek; week++) {
            // Calcula o primeiro dia da semana (segunda-feira) da semana ISO
            LocalDate startDate = LocalDate
                    .of(year, 1, 4)
                    .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = startDate.plusDays(6).atTime(23, 59, 59);

            List<RegisterHoras> registros;
            if (estagiario != null && !estagiario.isBlank()) {
                registros = registroHorasRepository.findByEstagiarioAndDataInicioBetween(estagiario.toLowerCase().trim(), start, end);
            } else {
                registros = registroHorasRepository.findByDataInicioBetween(start, end);
            }

            double total = registros.stream()
                    .mapToDouble(RegisterHoras::getHorasTrabalhadas)
                    .sum();

            result.put(week, total);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public double getTotalHoursForUser(String estagiario) {
        if (estagiario == null || estagiario.isBlank()) {
            // Se não for fornecido estagiário, devolve 0 (poderia também lançar erro)
            return 0.0;
        }
        String normalized = estagiario.toLowerCase().trim();
        List<RegisterHoras> registros = registroHorasRepository.findByEstagiario(normalized);
        return registros.stream()
                .mapToDouble(RegisterHoras::getHorasTrabalhadas)
                .sum();
    }

    // Utilitário para formatar decimal para "H.mm" (ex.: 2.5 -> "2.30")
    public static String formatHorasAsHDotMM(double horasDecimal) {
        int horas = (int) Math.floor(horasDecimal);
        int minutos = (int) Math.round((horasDecimal - horas) * 60.0);
        if (minutos == 60) {
            horas += 1;
            minutos = 0;
        }
        return String.format("%d.%02d", horas, minutos);
    }

    // ==================== MÉTODOS AUXILIARES ====================
    private Usuario getUsuario(String request) {
        return usuarioRepository.findByUsername(request)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado: {}", request);
                    return new RuntimeException("Usuário não encontrado: " + request);
                });
    }

    private void updateEstagiario(RegisterHoras registerHoras, String novoEstagiario) {
        Usuario usuario = getUsuario(novoEstagiario);

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
    private double calculateHoursBetween(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataFim.isBefore(dataInicio)) {
            log.error("Data fim ({}) é anterior à data início ({})", dataFim, dataInicio);
            throw new RuntimeException("Data fim não pode ser anterior à data início");
        }
        Duration duration = Duration.between(dataInicio, dataFim);
        long horasInteiras = duration.toHours();
        long minutos = duration.toMinutes() % 60;
        double horas = horasInteiras + (minutos / 60.0);

        if (horas > 24) {
            log.warn("Horas calculadas excedem 24 horas: {} horas", horas);
        }

        return horas;
    }
}
