package com.wrapper.apibridge.exception;

import lombok.experimental.StandardException;

@StandardException
public class NationalIDBirthDayDoesNotMatchException extends FinnotechClientException {
    public NationalIDBirthDayDoesNotMatchException() {
        super("تاریخ تولد یا کد ملی اشتباه است");
    }
}
