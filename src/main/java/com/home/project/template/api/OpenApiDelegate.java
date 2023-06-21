package com.home.project.template.api;

import com.home.project.template.openapi.api.ApiApiDelegate;
import com.home.project.template.openapi.model.MessageCreateRequest;
import com.home.project.template.openapi.model.MessageDto;
import com.home.project.template.openapi.model.MessagePageDto;
import com.home.project.template.openapi.model.MessageUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenApiDelegate implements ApiApiDelegate {

    @Override
    public ResponseEntity<MessageDto> createMessage(MessageCreateRequest messageCreateRequest) {
        return ApiApiDelegate.super.createMessage(messageCreateRequest);
    }

    @Override
    public ResponseEntity<Void> deleteMessageById(Long messageId) {
        return ApiApiDelegate.super.deleteMessageById(messageId);
    }

    @Override
    public ResponseEntity<MessagePageDto> getMessage(Integer page, Integer size, String sort) {
        return ApiApiDelegate.super.getMessage(page, size, sort);
    }

    @Override
    public ResponseEntity<MessageDto> updateMessage(Long messageId, MessageUpdateRequest messageUpdateRequest) {
        return ApiApiDelegate.super.updateMessage(messageId, messageUpdateRequest);
    }

}
