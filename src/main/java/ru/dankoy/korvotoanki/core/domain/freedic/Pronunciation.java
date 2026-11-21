package ru.dankoy.korvotoanki.core.domain.freedic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Pronunciation {

  private String type;

  private String text;

  private List<String> tags;
}
