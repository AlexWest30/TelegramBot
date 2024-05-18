package de.sindeev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import de.sindeev.entity.AppPhoto;

public interface AppPhotoDAO extends JpaRepository<AppPhoto, Long> {
}
