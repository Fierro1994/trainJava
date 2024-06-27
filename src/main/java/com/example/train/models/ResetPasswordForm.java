package com.example.train.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ResetPasswordForm {
    private String newPassword;
    private String confirmPassword;
}
