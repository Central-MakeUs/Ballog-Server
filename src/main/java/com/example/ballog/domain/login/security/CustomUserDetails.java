package com.example.ballog.domain.login.security;

import lombok.Getter;
import com.example.ballog.domain.login.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 필요한 권한을 반환할 수 있음
    }

    @Override
    public String getPassword() {
        return null; //비밀번호가 없기때문에 null값 반환
    }

    @Override
    public String getUsername() { //중복될 일이 없는 걸로 -> email or pk값으로 설정하는게 일반적
        return String.valueOf(user.getUserId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; //잠기지 않음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
