package com.wrapper.apibridge.service;

import com.wrapper.apibridge.exception.MessageServiceException;

import java.util.List;

public interface MessageService {
    record SendMessageResult(
            MessageStatus status,
            String messageId,
            String sender,
            String receptor,
            Long date,
            Integer cost
    ) {
    }

    enum MessageStatus {
        SCHEDULED,
        PROCESSING,
        UNDELIVERED, // Sent but receptor haven't got it yet (SMSService: receptor phone is off)
        DELIVERED,
        FAILED,
        UNKNOWN
    }

    SendMessageResult send(String receptor, String message) throws MessageServiceException;

    List<SendMessageResult> send(List<String> receptors, String message) throws MessageServiceException;

    SendMessageResult scheduleSend(String receptor, String message, Long date) throws MessageServiceException;

    List<SendMessageResult> scheduleSend(List<String> receptors, String message, Long date) throws MessageServiceException;

    List<SendMessageResult> multiSend(List<String> receptors, List<String> messages) throws MessageServiceException;

    List<SendMessageResult> scheduleMultiSend(List<String> receptors, List<String> messages, Long date) throws MessageServiceException;

    MessageStatus getMessageStatus(String messageId);

    MessageStatus cancelScheduledMessage(String messageId);
}
