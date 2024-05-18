package de.sindeev.service;

import de.sindeev.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
