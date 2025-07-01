package com.example.ballog.domain.login.security;

import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Long id = Long.valueOf(userId);


        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다. : " + userId));



        return new CustomUserDetails(user);
    }
}