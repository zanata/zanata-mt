# To test

Run:

- Database: `mvn docker:start`
- Wildfly:  `mvn clean verify cargo:run`

- Go to [test page](/) and follow instructions


# Available API

- {POST} /translate?sourceLang={sourceLocale}&targetLang={targetLocale} 
   - Body {
   url: String,
   title: String,
   divContent: String }
