package com.dswan.mtg.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "data_version")
public class DataVersionEntity {
    @Id
    private String id;
    @Column(name = "last_refresh")
    private LocalDateTime lastRefresh;
}
