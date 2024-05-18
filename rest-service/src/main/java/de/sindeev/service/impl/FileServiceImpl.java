package de.sindeev.service.impl;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import de.sindeev.dao.AppDocumentDAO;
import de.sindeev.dao.AppPhotoDAO;
import de.sindeev.entity.AppDocument;
import de.sindeev.entity.AppPhoto;
import de.sindeev.entity.BinaryContent;
import de.sindeev.service.FileService;
import de.sindeev.utils.CryptoTool;

import java.io.File;
import java.io.IOException;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, AppPhotoDAO appPhotoDAO, CryptoTool cryptoTool) {
		this.appDocumentDAO = appDocumentDAO;
		this.appPhotoDAO = appPhotoDAO;
		this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument getDocument(String hash) {
        var id = cryptoTool.idOf(hash);
        if (id == null) {
            return null;
		}
		return appDocumentDAO.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String hash) {
		var id = cryptoTool.idOf(hash);
		if (id == null) {
		    return null;
		}
		return appPhotoDAO.findById(id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            File temp = File.createTempFile("tempFile", ".bin");
            temp.deleteOnExit();
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileAsArrayOfBytes());
            return new FileSystemResource(temp);
		} catch (IOException e) {
		    log.error(e);
		    return null;
		}
    }
}
