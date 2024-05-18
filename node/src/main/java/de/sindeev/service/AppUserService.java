package de.sindeev.service;

import de.sindeev.entity.AppUser;

public interface AppUserService {
    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}
