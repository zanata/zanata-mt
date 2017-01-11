# To test (there is no access control, deploy only in localhost for testing)

Run:

- Database: `mvn docker:start`
- Wildfly:  `mvn clean verify cargo:run`

- Go to [test page](/) and follow instructions


# Available API

- {POST} /translate?sourceLang={sourceLocale}&targetLang={targetLocale} 
   - Body {
   url: String,
   titleText: String,
   contentHTML: String }
