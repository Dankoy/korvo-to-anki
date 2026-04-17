package ru.dankoy.korvotoanki.core.service.dictionaryapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.dankoy.korvotoanki.config.appprops.AppProperties;
import ru.dankoy.korvotoanki.config.appprops.DictionaryApiProperties;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Phonetics;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;

@DisplayName("Test DictionaryServiceOkHttp ")
@SpringBootTest(
    classes = {
      OkHttpClient.class,
      AppProperties.class,
      ObjectMapper.class,
      DictionaryServiceOkHttp.class
    })
@ExtendWith(MockitoExtension.class) // necessary for @Mock annotation to work
@TestPropertySource(properties = "korvo-to-anki.http-client=ok-http")
class DictionaryServiceOkHttpTest {

  @MockitoBean private OkHttpClient okHttpClient;

  @Mock private Call call;

  @Mock private Response response;

  @Mock private ResponseBody responseBody;

  @MockitoBean private DictionaryApiProperties dictionaryApiProperties;

  @Autowired private DictionaryService dictionaryService;

  @DisplayName("correct translation")
  @Test
  void defineCorrectWord() throws IOException {

    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn("https://whatever.com/");
    var word = "word";

    given(okHttpClient.newCall(any())).willReturn(call);
    given(call.execute()).willReturn(response);
    given(response.body()).willReturn(responseBody);
    given(response.isSuccessful()).willReturn(true);
    given(responseBody.string()).willReturn(getBody(true));

    List<Word> actual = dictionaryService.define(word);

    assertThat(actual).isEqualTo(getWords(false));

    Mockito.verify(dictionaryApiProperties, times(1)).getDictionaryApiUrl();
  }

  @DisplayName("definition throws exception")
  @Test
  void defineNonCorrectWordThrowsException() throws IOException {

    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn("https://whatever.com/");
    var word = "exception_word";

    given(okHttpClient.newCall(any())).willReturn(call);
    given(call.execute()).willReturn(response);
    given(response.body()).willReturn(responseBody);
    given(response.isSuccessful()).willReturn(false);
    given(response.code()).willReturn(404);
    given(responseBody.string()).willReturn(getBody(false));

    assertThatThrownBy(() -> dictionaryService.define(word))
        .isInstanceOf(DictionaryApiException.class);

    Mockito.verify(dictionaryApiProperties, times(1)).getDictionaryApiUrl();
  }

  @DisplayName("definition response body is null")
  @Test
  void defineResponseBodyNull() throws IOException {

    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn("https://whatever.com/");
    var word = "exception_word";

    given(okHttpClient.newCall(any())).willReturn(call);
    given(call.execute()).willReturn(response);
    given(response.isSuccessful()).willReturn(true);

    List<Word> actual = dictionaryService.define(word);

    assertThat(actual).isEqualTo(getWords(true));

    Mockito.verify(dictionaryApiProperties, times(1)).getDictionaryApiUrl();
  }

  private List<Word> getWords(boolean isEmpty) {

    if (isEmpty) {
      return Collections.singletonList(Word.emptyWord());
    } else {

      return Stream.of(
              new Word(
                  "data",
                  "phonetic",
                  Stream.of(new Phonetics("text", "audio", "source")).toList(),
                  Stream.of(
                          new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                              "ps",
                              Stream.of(
                                      new ru.dankoy.korvotoanki.core.domain.dictionaryapi
                                          .Definition(
                                          "info",
                                          Stream.of("synonym1").toList(),
                                          Stream.of("antonym1").toList(),
                                          "example"))
                                  .toList(),
                              Stream.of("synonym1").toList(),
                              Stream.of("antonym1").toList()))
                      .toList()))
          .toList();
    }
  }

  private String getBody(boolean isCorrect) {

    if (isCorrect) {
      return "[{\"word\":\"data\",\"phonetic\":\"phonetic\",\"phonetics\":[{\"text\":\"text\",\"audio\":\"audio\",\"sourceUrl\":\"source\"}],\"meanings\":[{\"partOfSpeech\":\"ps\",\"definitions\":[{\"definition\":\"info\",\"synonyms\":[\"synonym1\"],\"antonyms\":[\"antonym1\"],\"example\":\"example\"}],\"synonyms\":[\"synonym1\"],\"antonyms\":[\"antonym1\"]}]}]";
    } else {
      return "{\"title\":\"No Definitions Found\",\"message\":\"Sorry pal, we couldn't find"
          + " definitions for the word you were looking for.\",\"resolution\":\"You can try"
          + " the search again at later time or head to the web instead.\"}";
    }
  }
}
