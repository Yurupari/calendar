package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.model.dto.UserDto;
import com.yurupari.calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    public void validateUser(UserDto userDto) {
        Optional.ofNullable(userDto.email())
                .filter(StringUtils::hasText)
                .ifPresent(this::validateEmailFormat);
    }

    private void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidFormatException(String.format("Invalid format: email=%s", email));
        }
    }
}
