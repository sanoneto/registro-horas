package com.registo.horas_estagio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta paginada")
public record PageResponse<T>(
        @Schema(description = "Conteúdo da página")
        List<T> content,

        @Schema(description = "Número da página atual (0-based)")
        int pageNumber,

        @Schema(description = "Tamanho da página")
        int pageSize,

        @Schema(description = "Total de elementos")
        long totalElements,

        @Schema(description = "Total de páginas")
        int totalPages,

        @Schema(description = "É a primeira página?")
        boolean first,

        @Schema(description = "É a última página?")
        boolean last
) {
    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize,
                                         long totalElements, int totalPages,
                                         boolean first, boolean last) {
        return new PageResponse<>(content, pageNumber, pageSize, totalElements,
                totalPages, first, last);
    }
}