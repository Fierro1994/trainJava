package com.example.train.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ChangePasswordForm {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

}