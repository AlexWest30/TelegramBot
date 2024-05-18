package de.sindeev.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import de.sindeev.dao.AppUserDAO;
import de.sindeev.dao.RawDataDAO;
import de.sindeev.entity.AppDocument;
import de.sindeev.entity.AppPhoto;
import de.sindeev.entity.AppUser;
import de.sindeev.entity.RawData;
import de.sindeev.exceptions.UploadFileException;
import de.sindeev.service.AppUserService;
import de.sindeev.service.FileService;
import de.sindeev.service.MainService;
import de.sindeev.service.ProducerService;
import de.sindeev.service.enums.LinkType;
import de.sindeev.service.enums.ServiceCommand;

import static de.sindeev.entity.enums.UserState.BASIC_STATE;
import static de.sindeev.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static de.sindeev.service.enums.ServiceCommand.*;

@Log4j
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;
    private final AppUserService appUserService;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO,
		    FileService fileService, AppUserService appUserService) {
	this.rawDataDAO = rawDataDAO;
	this.producerService = producerService;
	this.appUserDAO = appUserDAO;
	this.fileService = fileService;
	this.appUserService = appUserService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

		var serviceCommand = ServiceCommand.fromValue(text);
		if (CANCEL.equals(serviceCommand)) {
	            output = cancelProcess(appUser);
		} else if (BASIC_STATE.equals(userState)) {
	            output = processServiceCommand(appUser, text);
		} else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
	            output = appUserService.setEmail(appUser, text);
		} else {
	            log.error("Unknown user state: " + userState);
	            output = "Unknown error. Enter /cancel and try again.";
		}

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
		saveRawData(update);
		var appUser = findOrSaveAppUser(update);
		var chatId = update.getMessage().getChatId();
		if (isNotAllowToSendContent(chatId, appUser)) {
		    return;
		}
	
		try {
		    AppDocument doc = fileService.processDoc(update.getMessage());
		    String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
		    var answer = "Document successfully uploaded! "
				    + "Link to download: " + link;
		    sendAnswer(answer, chatId);
		} catch (UploadFileException ex) {
		    log.error(ex);
		    String error = "Failed to upload the file. Try again later.";
		    sendAnswer(error, chatId);
		}
    }

    @Override
    public void processPhotoMessage(Update update) {
		saveRawData(update);
		var appUser = findOrSaveAppUser(update);
		var chatId = update.getMessage().getChatId();
		if (isNotAllowToSendContent(chatId, appUser)) {
		    return;
		}
	
		try {
		    AppPhoto photo = fileService.processPhoto(update.getMessage());
		    String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
		    var answer = "Photo successfully uploaded! "
				    + "Link to download: " + link;
		    sendAnswer(answer, chatId);
		} catch (UploadFileException ex) {
		    log.error(ex);
		    String error = "Failed to upload the photo. Try again later.";
		    sendAnswer(error, chatId);
		}
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Please, register you account first "
			    + "to upload the content.";
            sendAnswer(error, chatId);
            return true;
		} else if (!BASIC_STATE.equals(userState)) {
	            var error = "Please, cancel the current command with /cancel to upload content.";
		    sendAnswer(error, chatId);
		    return true;
		}
	        return false;
	    }
	
    private void sendAnswer(String output, Long chatId) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(output);
		producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = ServiceCommand.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
		    return appUserService.registerUser(appUser);
		} else if (HELP.equals(serviceCommand)) {
	            return help();
		} else if (START.equals(serviceCommand)) {
	            return "Welcome! To see the available commands please enter /help";
		} else {
		    return "Unknown command! To see the available commands please enter /help";
		}
    }

    private String help() {
        return "Available commands:\n"
			+ "/cancel - cancel the execution of the current command;\n"
			+ "/registration - register a user.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "The command was cancelled.";
    }

    private AppUser findOrSaveAppUser(Update update) {
		User telegramUser = update.getMessage().getFrom();
		var optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
	        if (optional.isEmpty()) {
	            AppUser transientAppUser = AppUser.builder()
				    .telegramUserId(telegramUser.getId())
				    .username(telegramUser.getUserName())
				    .firstName(telegramUser.getFirstName())
				    .lastName(telegramUser.getLastName())
				    .isActive(false)
				    .state(BASIC_STATE)
				    .build();
	            return appUserDAO.save(transientAppUser);
		}
	        return optional.get();
    }

    private void saveRawData(Update update) {
		RawData rawData = RawData.builder()
				.event(update)
				.build();
		rawDataDAO.save(rawData);
    }

}
