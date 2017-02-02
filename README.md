# To test (there is no access control, deploy only in localhost for testing)

Run:

- Database: `mvn docker:start`
- Wildfly:  `mvn clean verify cargo:run`

- Go to [index](/) and follow instructions

# Environment variables

- `ZANATA_MT_ID` (required) - Credentials ID for /api request
- `ZANATA_MT_API_KEY` (required) - Credentials Api key for /api request
- `ZANATA_MT_ORIGIN_WHITELIST` (optional) - Allowed url for CORS access control
- `ZANATA_MT_AZURE_ID` (required for MS engine) - Credentials for MS translators
- `ZANATA_MT_AZURE_SECRET` (required for MS engine) - Credentials for MS translators

# Available API

- {POST} /translate?targetLang={targetLocale}&inlineAttribution={true|false} 
   - Headers
     - X-Auth-User {ZANATA_MT_ID}
     - X-Auth-Token {ZANATA_MT_API_KEY}
   - Body 
   ```
   {
     url: String {url of article},
     titleText: String {title text of the page},
     contentHTML: String {html content},
     articleType: String {'KCS_ARTICLE'},
     locale: String {source locale id},
   }
   ```