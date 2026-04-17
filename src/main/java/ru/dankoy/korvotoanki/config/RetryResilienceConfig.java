package ru.dankoy.korvotoanki.config;

import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RetryResilienceConfig {

  @Bean
  public RegistryEventConsumer<Retry> myRetryRegistryEventConsumer() {

    return new RegistryEventConsumer<Retry>() {
      @Override
      public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
        entryAddedEvent
            .getAddedEntry()
            .getEventPublisher()
            .onEvent(event -> log.info(event.toString()));
      }

      @Override
      public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {
        entryRemoveEvent
            .getRemovedEntry()
            .getEventPublisher()
            .onEvent(event -> log.info(event.toString()));
      }

      @Override
      public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {
        entryReplacedEvent
            .getOldEntry()
            .getEventPublisher()
            .onEvent(event -> log.info(event.toString()));
        entryReplacedEvent
            .getNewEntry()
            .getEventPublisher()
            .onEvent(event -> log.info(event.toString()));
      }
    };
  }
}
