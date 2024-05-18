package de.sindeev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import de.sindeev.entity.AppDocument;

public interface AppDocumentDAO extends JpaRepository<AppDocument, Long> {
}
