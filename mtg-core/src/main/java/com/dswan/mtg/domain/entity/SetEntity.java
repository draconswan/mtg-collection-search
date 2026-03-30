package com.dswan.mtg.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "card_set")
@Data
public class SetEntity {

    @Id
    private String code;

    private String name;
    private LocalDate releasedAt;

    @Column(columnDefinition = "TEXT")
    private String iconSvg; // store raw SVG markup

}