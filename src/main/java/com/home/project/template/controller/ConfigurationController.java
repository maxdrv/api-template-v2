package com.home.project.template.controller;

import com.home.project.template.configuration.Configuration;
import com.home.project.template.configuration.ConfigurationList;
import com.home.project.template.configuration.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationRepository configurationRepository;

    @GetMapping("/configuration/list")
    public ResponseEntity<ConfigurationList> findAll() {
        List<Configuration> all = configurationRepository.findAll();
        return ResponseEntity.ok(new ConfigurationList(all));
    }

    @GetMapping("/configuration/find")
    public ResponseEntity<Configuration> find(@RequestParam("key") String key) {
        if (key == null) {
            throw new RuntimeException("key is null");
        }
        Optional<Configuration> configuration = configurationRepository.findByKey(key);
        return configuration.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/configuration/upsert")
    public ResponseEntity<Void> upsert(@RequestParam("key") String key, @RequestParam("value") String value) {
        if (key == null) {
            throw new RuntimeException("key is null");
        }
        if (value == null) {
            throw new RuntimeException("value is null");
        }
        configurationRepository.upsert(key, value);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/configuration/delete")
    public ResponseEntity<Void> delete(@RequestParam("key") String key) {
        if (key == null) {
            throw new RuntimeException("key is null");
        }
        configurationRepository.delete(key);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
