# To test (there is no access control, deploy only in localhost for testing)

Run:

- Database: `mvn docker:start` (This will create docker image **zanataMTDB**)
- Wildfly:  `mvn clean verify cargo:run`

----

# Documentations

- [API](/docs/api.md)
- [System properties](/docs/system-properties.md)
