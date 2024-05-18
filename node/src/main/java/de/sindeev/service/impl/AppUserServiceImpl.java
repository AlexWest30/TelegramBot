package de.sindeev.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import de.sindeev.dao.AppUserDAO;
import de.sindeev.dto.MailParams;
import de.sindeev.entity.AppUser;
import de.sindeev.entity.enums.UserState;
import de.sindeev.service.AppUserService;
import de.sindeev.utils.CryptoTool;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static de.sindeev.entity.enums.UserState.BASIC_STATE;
import static de.sindeev.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Log4j
@Service
public class AppUserServiceImpl implements AppUserService {
	
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
		this.appUserDAO = appUserDAO;
		this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "You are already registered!";
		} else if (appUser.getEmail() != null) {
	            return "You have already received the registration email. "
				    + "Follow the link in the email to finish the registration.";
		}
	        appUser.setState(WAIT_FOR_EMAIL_STATE);
	        appUserDAO.save(appUser);
		return "Enter your email:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
		    InternetAddress emailAddr = new InternetAddress(email);
		    emailAddr.validate();
		} catch (AddressException e) {
		    return "Please, enter a valid email. To cancel registration enter /cancel";
		}
	        var optional = appUserDAO.findByEmail(email);
	        if (optional.isEmpty()) {
	            appUser.setEmail(email);
	            appUser.setState(BASIC_STATE);
	            appUser = appUserDAO.save(appUser);
	
	            var cryptoUserId = cryptoTool.hashOf(appUser.getId());
	            var response = sendRequestToMailService(cryptoUserId, email);
	            if (response.getStatusCode() != HttpStatus.OK) {
	                var msg = String.format("Failed to send the email to %s.", email);
	                log.error(msg);
	                appUser.setEmail(null);
	                appUserDAO.save(appUser);
	                return msg;
		    }
	            return "A registration mail was sent to your email address."
				    + "Follow the link in the email to finish the registration.";
		} else {
		    return "This email is already in use. Enter a valid email."
				    + " To cancel registration enter /cancel";
		}
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
			.id(cryptoUserId)
			.emailTo(email)
			.build();
        var request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
			HttpMethod.POST,
			request,
			String.class);
    }
}
