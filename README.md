# Migration Manager form ORM

Migration extension for [kotlin-orm](https://github.com/tcds-io/kotlin-orm)

--

#### change [gradle.properties](gradle.properties) to setup jdbc connection
```properties
migrations.jdbcUrl=${MY_JDBC_WRITE_URL_ENV}
```

#### change [gradle.properties](gradle.properties) to setup migration directories
```properties
# single migration directory configuration
migrations.directory=my/single/module/src/main/resources/.migrations

# multiple migration directory configuration
migrations.directory[first]=my/first/module/src/main/resources/.migrations
migrations.directory[second]=my/second/module/src/main/resources/.migrations
migrations.directory[third]=my/third/module/src/main/resources/.migrations
```
