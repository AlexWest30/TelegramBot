package de.sindeev.service.impl;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import de.sindeev.dto.MailParams;
import de.sindeev.service.MailSenderService;

@Service
public class MailSenderServiceImpl implements MailSenderService {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String activationServiceUri;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(MailParams mailParams) {
        var subject = "Account registration";
        var messageBody = getActivationMailBody(mailParams.getId());
        var emailTo = mailParams.getEmailTo();

//		SimpleMailMessage mailMessage = new SimpleMailMessage();
//		mailMessage.setFrom(emailFrom);
//		mailMessage.setTo(emailTo);
//		mailMessage.setSubject(subject);
//		mailMessage.setText(messageBody);
        
        MimeMessage mailMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "utf-8");
        try {
            helper.setText(messageBody, true); // Set to true to indicate HTML
            helper.setTo(emailTo);
            helper.setSubject(subject);
            helper.setFrom(emailFrom);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
	
		javaMailSender.send(mailMessage);
    }

    private String getActivationMailBody(String id) {
        var link = activationServiceUri.replace("{id}", id);
        var msg = String.format(
            "Dear Telegram Bot user,<br><br>" +
            "To finish the registration, please, click on the link:<br>" +
            "<a href=\"%s\">Link</a><br><br>" +
            "Kind regards,<br>Your Telegram Bot Support", link);
        return msg;
    }
}
