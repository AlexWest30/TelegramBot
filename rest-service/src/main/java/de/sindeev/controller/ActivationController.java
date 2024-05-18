package de.sindeev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import de.sindeev.service.UserActivationService;

@RequestMapping("/user")
@RestController
public class ActivationController {
    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
	this.userActivationService = userActivationService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String id) {
        var res = userActivationService.activation(id);
        if (res) {
            return ResponseEntity.ok().body("Registration was successfully completed.");
		}
	        return ResponseEntity.internalServerError().build();
	    }
}
