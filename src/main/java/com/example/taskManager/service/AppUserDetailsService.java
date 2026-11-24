package com.example.taskManager.service;

import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователя с такой почтой нет."));
        String passwordHash = user.getPasswordHash();
        if(passwordHash == null){
            throw new UsernameNotFoundException("У аккаунта нет данных для входа");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(passwordHash)
                .roles("USER")
                .build();

    }
}
