package com.dswan.mtg.domain.entity;

public record UserLandGroupReportDto(
        String cardName,
        String landGroup,
        Long totalCount,
        Long checkedCount,
        Long uncheckedCount
) {
}