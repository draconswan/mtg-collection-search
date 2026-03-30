package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.SetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetRepository extends JpaRepository<SetEntity, String> {}