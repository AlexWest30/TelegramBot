package de.sindeev.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import de.sindeev.entity.AppDocument;
import de.sindeev.entity.AppPhoto;
import de.sindeev.service.enums.LinkType;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
