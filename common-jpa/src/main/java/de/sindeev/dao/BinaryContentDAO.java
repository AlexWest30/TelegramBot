package de.sindeev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import de.sindeev.entity.BinaryContent;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long> {
}
