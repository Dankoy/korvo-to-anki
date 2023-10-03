package ru.dankoy.korvotoanki;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.dankoy.korvotoanki.core.service.exporter.ExporterServiceAnki;

@DisplayName("Test sync bean context ")
@SpringBootTest
@Import(ExporterServiceAnki.class)
@TestPropertySource(properties = "korvo-to-anki.async=false")
class KorvoToAnkiApplicationExporterSyncTests {

  @Autowired
  ApplicationContext context;

  @DisplayName("all sync exporter bean")
  @Test
  void contextLoads() {

    var exporterServiceAnki = context.getBean(ExporterServiceAnki.class);

    assertNotNull(exporterServiceAnki);


  }

}
