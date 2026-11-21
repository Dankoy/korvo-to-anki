## korvo-to-anki

[![GitHub Release](https://img.shields.io/github/v/release/dankoy/korvo-to-anki)](https://github.com/Dankoy/korvo-to-anki/releases/latest)
![GitHub Release Date](https://img.shields.io/github/release-date/dankoy/korvo-to-anki)
![Commits since last release](https://img.shields.io/github/commits-since/Dankoy/korvo-to-anki/latest/main)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/dankoy/korvo-to-anki/total)

[![Java CI with Gradle](https://github.com/Dankoy/korvo-to-anki/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/Dankoy/korvo-to-anki/actions/workflows/gradle.yml)

![Hits](https://hitscounter.dev/api/hit?url=https%3A%2F%2Fgithub.com%2FDankoy%2Fkorvo-to-anki&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)

![GitHub search hit counter](https://img.shields.io/github/search/dankoy/korvo-to-anki/korvo-to-anki)
![GitHub search hit counter](https://img.shields.io/github/search/dankoy/korvo-to-anki/anki)
![GitHub search hit counter](https://img.shields.io/github/search/dankoy/korvo-to-anki/koreader)
![GitHub search hit counter](https://img.shields.io/github/search/dankoy/korvo-to-anki/vocabulary)

Converts an sqlite database created
by [vocabulary_builder plugin](https://github.com/koreader/koreader/wiki/Vocabulary-builder)
of [KOReader](https://github.com/koreader/koreader) into a text file that can be imported into Anki.

If you want to lemmatize all your words, check out the companion utility -
[korvo-to-anki-lemmatizer](https://github.com/Dankoy/korvo-to-anki-lemmatizer)

> [!NOTE]
> If you have questions about running this utility, feel free to ask in [github discussions](https://github.com/Dankoy/korvo-to-anki/discussions) or in the [discord server](https://discord.gg/XnMZStkNMC)

### Features

* translations, transcriptions, definitions, synonyms, antonyms and examples on the card
* data merged from three sources: google translate, dictionaryapi.dev and freedictionaryapi.com (wiktionary)
* export all books at once or a chosen set of books (`--books`)
* incremental export: a state database remembers already exported words
* collapsible card sections (`<details>/<summary>`, no javascript) with truncated long synonym lists
* rate limiters and retries for every external api, so big exports don't die halfway

### Stack

* Java 21
* Spring boot 4, Spring shell, Spring boot jdbc
* WebClient
* Freemarker
* Liquibase, Flyway
* Caffeine cache
* Gradle
* Resilience4j (Retry, Rate Limiter)

### Integrations

External services used for translation and definition lookup:

1. Google Translate ([see also](https://koreader.rocks/doc/modules/ui.translator.html))
2. [dictionaryapi.dev](https://dictionaryapi.dev/) - english only
3. [freedictionaryapi.com](https://freedictionaryapi.com/) - wiktionary data, any language

Each integration can be switched off, see [configuration](#configuration).

### Usage

Download app.jar.zip from [releases](https://github.com/Dankoy/korvo-to-anki/releases) and extract the jar somewhere.

JDK 21 must be installed. See [official Oracle instructions](https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html) for the installation process.

Point the app at your KOReader vocabulary database and run the export command:

#### On linux

```shell
java -jar -Dspring.datasource.url=jdbc:sqlite:/path/to/vocabulary_builder.sqlite3 korvo-to-anki.jar
```

#### On windows

```shell
java "-Dspring.datasource.url=jdbc:sqlite:\path\to\vocabulary_builder.sqlite3" -jar .\korvo-to-anki.jar
```

The app starts an interactive shell. Export everything:

```text
shell:>ae
```

Export only chosen books (semicolon-separated; names may contain commas and spaces):

```text
shell:>ae --books "The Darkness That Comes Before;Orcs"
shell:>ae --books "Moby Dick, or the Whale"
```

Translate between other languages:

```text
shell:>ae --sourceLanguage ja --targetLanguage en
```

#### Export command options

```text
SYNOPSIS
       anki-exporter --sourceLanguage String --targetLanguage String --options String[] --books String

OPTIONS
       --sourceLanguage String   [Optional, default = auto]
       --targetLanguage String   [Optional, default = ru]
       --options String[]        google translate options
                               [Optional, default = t,at,md,rm,ss,ex,qca]
       --books String            semicolon-separated book names; when set, only words of
                               these books are exported and the state is tracked per book
                               [Optional, default = all books]
```

`--options` are google translate `dt` options. Currently `t`, `at`, `md`, `rm`, `ss`, `ex` and `qca`
are parsed and used. The rest (`bd`, `ld`, `rw`) could be requested but are not parsed.
Options `ss`, `ex` and `qca` only give additional results when the text is a single word.
When `qca` finds a possible mistake, it is logged during export.

### Run result

The export is stored next to the jar as **export-korvo-to-anki-TIMESTAMP.txt**.
The app also creates **korvo-to-anki-state.sqlite3** with the words already exported; on the next
run those words are filtered out, so repeated exports only contain new words.

When imported into Anki, a deck `korvo-to-anki` is created with a subdeck per book. Every card gets
tags for its parts of speech (noun, verb, ...) and the book title. Duplicates are checked by Anki
on import.

### How card data is assembled

The card has two levels of meanings. Synonyms and antonyms exist on both levels, examples are
kept separately because they are not bound to a part of speech:

```
AnkiData
├── word, transcription, translations, myExample
├── meanings - level of part of speech (noun, verb, ...)
│   └── Meaning
│       ├── type
│       ├── definitions - level of definition
│       │   └── Definition
│       │       ├── info
│       │       ├── example - usage example of this definition
│       │       ├── synonyms
│       │       └── antonyms
│       ├── synonyms
│       └── antonyms
└── examples - usage example sentences not bound to a part of speech
```

Data is merged from the three sources in a fixed order:

1. **dictionaryapi.dev is the base.** Its meanings per part of speech are used as-is.
2. **Google translate is merged in.** GT definitions of the same part of speech are appended
   with their examples and synonyms; a GT definition that exactly duplicates a dictionaryapi
   definition is merged instead (synonyms are unioned, the GT example is used when the base
   definition has none). Meaning level synonyms are the union of both sources. Parts of speech
   that only GT has are added as extra meanings. GT always provides the translations, the
   `examples` section and the possible-mistake hint.
3. **freedictionaryapi is merged on top.** Its IPA transcription has the highest priority. Its
   wiktionary senses (subsenses flattened) are appended to the definitions of the same part of
   speech - a duplicate sense is merged instead (synonyms/antonyms unioned, first sense example
   used when the existing definition has none). Quotation references like
   `1850s, Author, Work` or `Quote — Author, Work (Publisher 2006)` are stripped from wiktionary
   examples - only the sentence stays. Remaining sense examples go to the `examples` section.
   Parts of speech that only freedictionaryapi has are added as extra meanings.

Protections applied along the way:

* exact duplicate meanings/definitions across sources are merged, not repeated
* synonym/antonym lists are capped on both levels so the card doesn't become overloaded;
  the first items are shown, the rest hides behind a clickable `more N` toggle
* blank google translate definitions and dictionaryapi category headers like
  `(Physical movement.)` are skipped
* duplicates in the `examples` section are skipped (compared by normalized text against
  definition examples of both sources and the book example sentence)

If a word is not found in dictionaryapi.dev, only the GT (and freedictionaryapi) data is used -
GT at least has a translation, so meanings are never empty.

#### Rendering

The export is one-sided html: the front side is the word only, the back side is translations,
the book example sentence and the rendered `meanings` HTML. Column layout of the exported file:
deck, word, translations, book example, meanings HTML, tags. A reverse card is not generated.

* definition level synonyms and antonyms are rendered inside the definition block, indented,
  with muted colors - examples gray, synonyms grayish green, antonyms grayish red
* meaning level synonyms and antonyms are rendered at the end of the part of speech block
  in green and red
* lists longer than 5 items are truncated with a `more N` toggle that expands them in place
* the `examples` section is separated by a horizontal line and is collapsible, open by default

Everything works in vanilla Anki (Chromium inside) and AnkiDroid - collapsible sections use
standard html5 `<details>/<summary>`, no javascript.

### Configuration

Startup properties:

| Property | Values | Default | Description |
|----------|--------|---------|-------------|
| `korvo-to-anki.api.dictionaryApiEnabled` | `true`/`false` | `true` | dictionaryapi.dev integration |
| `korvo-to-anki.api.freeDictionaryApiEnabled` | `true`/`false` | `true` | freedictionaryapi.com integration |
| `korvo-to-anki.async-type` | `vtcf`/`completable_future` | `vtcf` | async core implementation |
| `spring.datasource.url` | jdbc url | `jdbc:sqlite:vocabulary_builder.sqlite3` | KOReader vocabulary database |
| `spring.datasource.state.url` | jdbc url | `jdbc:sqlite:korvo-to-anki-state.sqlite3` | export state database |

### Limits

Every external api call goes through a local limiter and retry, so the app should never trip a
remote rate limit:

| Api | Local limiter |
|-----|---------------|
| dictionaryapi.dev | 5 requests / 5 seconds |
| freedictionaryapi.com | 15 requests / 60 seconds (api cap: 1,000 requests / hour per IP) |
| google translate | no limiter, 3 retries with 1s wait on transient failures |

The bottleneck of a big export is freedictionaryapi: 900 words per hour max. Rough estimates:
a 300-word book takes ~30 minutes, 4,000 words - around 4.5 hours.

dictionaryapi.dev works only with english source words; freedictionaryapi accepts any language
(the export source language is passed to it, lookup is skipped when the source language is `auto`).

### Show definitions in cards

The export contains html for definitions, synonyms, antonyms and examples. To show them on the
card, change the card templates.

Back template:

```text
{{FrontSide}}

<hr id=answer>

{{Back}}

<br><br>
{{hint:ExampleFromBook}}

<br><br>
{{hint:Meaning}}
```

Flipped front template:

```text
{{Back}}

<br><br>
{{hint:ExampleFromBook}}

<br><br>
{{hint:Meaning}}
```

### Example result

One line of the export file looks like this (formatted for readability - the real file has one
card per line, `#separator:pipe` `#html:true` headers):

```text
korvo-to-anki::The Dark Forest|disregard|игнорировать, не обращать внимания|the Milky Way could be thousands of times that...|<p> transcription: /dɪsɹɪˈɡɑːd/</p><ul><li><span style="color:yellow">noun</span><dl>... definitions, examples, synonyms ...</dl></li><li><span style="color:yellow">verb</span>...</li></ul>|korvo-to-anki::noun korvo-to-anki::verb korvo-to-anki::the_dark_forest
```

### Example

![img.png](screenshots/img.png)
![img.png](screenshots/img2.png)
![img.png](screenshots/img3.png)
![img.png](screenshots/img4.png)
