package net.siudek.media;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

public interface CommandsListener {
    
    void on(MediaCommands command);
}

@Component
@Slf4j
class LoggingCommandsListener implements CommandsListener {

    @Override
    public void on(MediaCommands command) {
        log.info("Command: {}", command);
    }
}
