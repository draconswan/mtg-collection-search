package com.dswan.mtg.repository;

import com.dswan.mtg.domain.DataVersion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataVersionRepository extends CrudRepository<DataVersion, String> {
}
