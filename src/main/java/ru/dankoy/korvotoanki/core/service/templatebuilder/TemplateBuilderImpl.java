package ru.dankoy.korvotoanki.core.service.templatebuilder;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;

@Getter
@RequiredArgsConstructor
public class TemplateBuilderImpl implements TemplateBuilder {

  private final Configuration configuration;

  /**
   * Конструктор заполняющий конфигурацию шаблонизатора
   *
   * @param templatesDir директория в которой лежат искомые шаблоны.
   */
  public TemplateBuilderImpl(String templatesDir) {
    configuration = new Configuration(Configuration.VERSION_2_3_29);
    // configuration.setDirectoryForTemplateLoading(new File(templatesDir)); // for directory
    configuration.setClassForTemplateLoading(this.getClass(), templatesDir); // for resource
    configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
  }

  /**
   * Заполнение шаблона данными
   *
   * @param templateData данные для заполнения шаблона в формате {@code Map<String, Object>}
   * @param templateName имя шаблона
   * @return заполненный шаблон в виде строки
   */
  @Override
  public String writeTemplate(Map<String, Object> templateData, String templateName) {

    try (Writer stream = new StringWriter()) {

      Template template = configuration.getTemplate(templateName);

      template.process(templateData, stream);
      return stream.toString();

    } catch (Exception e) {

      throw new KorvoRootException(String.format("Couldn't process template '%s'", templateName),
          e);
    }
  }

  /**
   * Позволяет сделать шаблон из строки, а не из файла
   *
   * @param templateName   имя шаблона для добавления в конфигурацию шаблонизатора
   * @param templateString строка шаблона
   * @param templateData   данные загружаемые в шаблон
   * @return отформатированный шаблон
   */
  @Override
  public String loadTemplateFromString(String templateName, String templateString, Map<String,
      Object> templateData) {

    try (Writer stream = new StringWriter()) {
      Template template = new Template(templateName, new StringReader(templateString),
          configuration);
      template.process(templateData, stream);
      return stream.toString();

    } catch (Exception e) {

      throw new KorvoRootException(String.format("Couldn't process template '%s'", templateName),
          e);
    }

  }

}
