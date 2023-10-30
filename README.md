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
migrations.directory=my/migration/folder

# multiple migration directory configuration
migrations.directory[0]=my/first/migration/folder
migrations.directory[1]=my/second/migration/folder
migrations.directory[2]=my/third/migration/folder
```
