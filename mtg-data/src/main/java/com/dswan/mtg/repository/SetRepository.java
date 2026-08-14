package com.dswan.mtg.repository;

import com.dswan.mtg.domain.entity.SetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetRepository extends JpaRepository<SetEntity, String> {}