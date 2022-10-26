package com.vmanam.recomendr.controllers;

import com.vmanam.recomendr.entities.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/publish")
public class UserEventsController {
    @Autowired
    private KafkaTemplate template;

    @PostMapping("/userevent")
    public ResponseEntity publishUserEvent(@RequestBody UserEvent event) {
        template.send("user-events", event.getUserId(), event).addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("FAILURE: " + event.getUserId() + " " + ex.getMessage());
            }

            @Override
            public void onSuccess(Object result) {
                SendResult res = (SendResult) result;
                System.out.println("SUCCESS: " + event.getUserId() + " " + res.getRecordMetadata().partition() + " " + res.getRecordMetadata().timestamp());
            }
        });
        return ResponseEntity.ok().build();

    }
}
