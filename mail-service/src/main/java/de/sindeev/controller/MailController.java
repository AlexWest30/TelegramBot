package de.sindeev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import de.sindeev.dto.MailParams;
import de.sindeev.service.MailSenderService;

@RequestMapping("/mail")
@RestController
public class MailController {
    private final MailSenderService mailSenderService;

    public MailController(MailSenderService mailSenderService) {
	this.mailSenderService = mailSenderService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParams mailParams) {
        mailSenderService.send(mailParams);
        return ResponseEntity.ok().build();
    }
}
