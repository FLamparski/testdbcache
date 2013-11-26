testdbcache
===========

This should really be just a gist, but the problem is that it's a complete (albeit small) Android project.

The purpose is to test SQLite-based caching of serializable objects. The premise is simple: there is one database, `onsCache`.
It is created using the following statement:

```sql
CREATE TABLE onsCache (
    id INTEGER AUTOINCREMENT PRIMARY KEY,
    url TEXT,
    dateRetrieved INTEGER,
    cachedObject BLOB
);
```

The idea is to speed up and offline-proof the reading of ONS data objects by storing them in a database.
They will also be stored with a retrieval time -- if the objects are older than a month, they will be updated
automatically unless there is no connection. A manual update option will be available in Areabase, which will
simply truncate the table.
