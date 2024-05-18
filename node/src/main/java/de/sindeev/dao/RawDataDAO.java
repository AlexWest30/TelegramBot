package de.sindeev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import de.sindeev.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
