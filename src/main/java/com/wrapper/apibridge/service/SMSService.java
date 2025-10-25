package com.wrapper.apibridge.service;

import com.kavenegar.sdk.KavenegarApi;
import com.kavenegar.sdk.enums.MessageType;
import com.kavenegar.sdk.excepctions.HttpException;
import com.kavenegar.sdk.models.SendResult;
import com.kavenegar.sdk.models.StatusResult;
import com.wrapper.apibridge.exception.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SMSService implements MessageService {
    private final KavenegarApi kavenegarApi;
    private final String senderNumber;

    public SMSService(
            @Value("${app.kavenegar.apiKey}")
            String kavenegarApiKey,
            @Value("${app.kavenegar.senderNumber}")
            String senderNumber
    ) {
        this.kavenegarApi = new KavenegarApi(kavenegarApiKey);
        this.senderNumber = senderNumber;
    }

    private static <T> T handleKavenegarExceptions(Supplier<T> supplier) throws MessageServiceException {
        try {
            return supplier.get();
        } catch (HttpException httpException) {
            switch (httpException.getCode()) {
                case 414:
                    throw new TooManyMessagesException();
                case 417:
                    throw new InvalidDateException();
                case 418:
                    throw new NotEnoughCreditsException();
                case 451:
                    throw new TooManyRequestsException("Too many requests are sent to kavenegar api");
                default:
                    throw new MessageServiceException(
                            "Unhandled http error code: " + httpException.getCode()
                    );
            }
        }
    }

    private SendMessageResult mapSendResult(SendResult result) {
        MessageStatus status = mapStatus(result.getStatus());

        return new SendMessageResult(
                status,
                String.valueOf(result.getMessageId()),
                result.getSender(),
                result.getReceptor(),
                result.getDate(),
                result.getCost()
        );
    }

    private MessageStatus mapStatus(int status) {
        return switch (status) {
            case 1, 3, 4 -> MessageStatus.PROCESSING;
            case 2 -> MessageStatus.SCHEDULED;
            case 11 -> MessageStatus.UNDELIVERED;
            case 10 -> MessageStatus.DELIVERED;
            case 6, 13, 14 -> MessageStatus.FAILED;
            default -> MessageStatus.UNKNOWN;
        };
    }

    @Override
    public SendMessageResult send(String receptor, String message) throws MessageServiceException {
        SendResult result = handleKavenegarExceptions(
                () -> kavenegarApi.send(senderNumber, receptor, message)
        );

        return mapSendResult(result);
    }

    @Override
    public List<SendMessageResult> send(List<String> receptors, String message) throws MessageServiceException {
        List<SendResult> results = handleKavenegarExceptions(
                () -> kavenegarApi.send(senderNumber, receptors, message)
        );

        return results.stream().map(this::mapSendResult).toList();
    }

    @Override
    public SendMessageResult scheduleSend(String receptor, String message, Long date) throws MessageServiceException {
        SendResult result = handleKavenegarExceptions(
                () -> kavenegarApi.send(senderNumber, receptor, message, MessageType.MobileMemory, date)
        );

        return mapSendResult(result);
    }

    @Override
    public List<SendMessageResult> scheduleSend(List<String> receptors, String message, Long date) throws MessageServiceException {
        List<SendResult> results = handleKavenegarExceptions(
                () -> kavenegarApi.send(senderNumber, receptors, message, MessageType.MobileMemory, date)
        );

        return results.stream().map(this::mapSendResult).toList();
    }

    @Override
    public List<SendMessageResult> multiSend(List<String> receptors, List<String> messages) throws MessageServiceException {
        List<SendResult> results = handleKavenegarExceptions(
                () -> kavenegarApi.sendArray(senderNumber, receptors, messages)
        );

        return results.stream().map(this::mapSendResult).toList();
    }

    @Override
    public List<SendMessageResult> scheduleMultiSend(List<String> receptors, List<String> messages, Long date) throws MessageServiceException {
        List<SendResult> results = handleKavenegarExceptions(
                () -> kavenegarApi.sendArray(senderNumber, receptors, messages, Collections.nCopies(receptors.size(), MessageType.MobileMemory), date, null)
        );

        return results.stream().map(this::mapSendResult).toList();
    }

    @Override
    public MessageStatus getMessageStatus(String messageId) {
        try {
            StatusResult status = handleKavenegarExceptions(() -> kavenegarApi.status(Long.valueOf(messageId)));
            if (status.getStatus() == null) {
                return MessageStatus.UNKNOWN;
            }
            return mapStatus(status.getStatus().getValue());
        } catch (NumberFormatException e) {
            throw new InvalidMessageIdFormat(e.getMessage());
        }
    }

    @Override
    public MessageStatus cancelScheduledMessage(String messageId) {
        try {
            StatusResult status = handleKavenegarExceptions(() -> kavenegarApi.cancel(Long.valueOf(messageId)));
            if (status.getStatus() == null) {
                return MessageStatus.UNKNOWN;
            }
            return mapStatus(status.getStatus().getValue());
        } catch (NumberFormatException e) {
            throw new InvalidMessageIdFormat(e.getMessage());
        }
    }
}