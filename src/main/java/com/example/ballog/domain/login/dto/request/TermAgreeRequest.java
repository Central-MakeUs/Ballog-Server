package com.example.ballog.domain.login.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermAgreeRequest {
    private boolean privacyAgree;
    private boolean serviceAgree;
    private boolean marketingAgree;
}
