package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;

public class BookResultSetExtractor implements ResultSetExtractor<Map<String, Vocabulary>> {

  @Override
  public Map<String, Vocabulary> extractData(ResultSet rs)
      throws SQLException, DataAccessException {

    Map<String, Vocabulary> words = new HashMap<>();
    Map<Long, Title> titles = new HashMap<>();

    while (rs.next()) {

      long titleId = rs.getLong("t_id");
      var titleName = rs.getString("t_name");
      var titleFilter = rs.getLong("t_filter");
      titles.computeIfAbsent(titleId, k -> new Title(titleId, titleName, titleFilter));

      String word = rs.getString("v_word");
      var vocabulary = words.get(word);
      if (vocabulary == null) {

        long createTime = rs.getLong("v_create_time");
        long reviewTime = rs.getLong("v_review_time");
        long dueTime = rs.getLong("v_due_time");
        long reviewCount = rs.getLong("v_review_count");
        String prevContext = rs.getString("v_prev_context");
        String nextContext = rs.getString("v_next_context");
        long streakCount = rs.getLong("v_streak_count");
        long wordTitleId = rs.getLong("v_title_id");

        var t = titles.get(wordTitleId);

        vocabulary =
            new Vocabulary(
                word,
                t,
                createTime,
                reviewTime,
                dueTime,
                reviewCount,
                prevContext,
                nextContext,
                streakCount);
        words.put(vocabulary.word(), vocabulary);
      }
    }
    return words;
  }
}
