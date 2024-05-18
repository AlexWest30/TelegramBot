package de.sindeev.service;

import org.springframework.core.io.FileSystemResource;
import de.sindeev.entity.AppDocument;
import de.sindeev.entity.AppPhoto;
import de.sindeev.entity.BinaryContent;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
