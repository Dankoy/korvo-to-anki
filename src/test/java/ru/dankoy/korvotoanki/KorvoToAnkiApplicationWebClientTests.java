package ru.dankoy.korvotoanki;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryServiceOkHttp;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslatorOkHttp;

@DisplayName("Test okhttp beans context ")
@SpringBootTest
@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
    })
@Import({GoogleTranslatorOkHttp.class, DictionaryServiceOkHttp.class})
@TestPropertySource(properties = "korvo-to-anki.http-client=ok-http")
class KorvoToAnkiApplicationWebClientTests {

  @Autowired ApplicationContext context;

  @DisplayName("all okhttp service beans")
  @Test
  void contextLoads() {

    var googleTranslatorOkHttp = context.getBean(GoogleTranslatorOkHttp.class);
    var dictionaryServiceOkHttp = context.getBean(DictionaryServiceOkHttp.class);

    assertNotNull(googleTranslatorOkHttp);
    assertNotNull(dictionaryServiceOkHttp);
  }
}
