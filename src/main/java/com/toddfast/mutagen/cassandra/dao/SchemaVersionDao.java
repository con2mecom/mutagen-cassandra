package com.toddfast.mutagen.cassandra.dao;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.toddfast.mutagen.cassandra.impl.SessionHolder;
import com.toddfast.mutagen.cassandra.table.SchemaConstants;
import com.toddfast.mutagen.cassandra.table.SchemaVersion;

import java.nio.ByteBuffer;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

public class SchemaVersionDao {

    private String tableCreationQuery = "CREATE TABLE IF NOT EXISTS %s.%s(key text, column1 text, value blob, PRIMARY KEY(key, column1));";
    private SessionHolder sessionHolder;
    private Mapper<SchemaVersion> schemaVersionMapper;

    public SchemaVersionDao(SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
        createSchemaVersionTable();
        this.schemaVersionMapper = new MappingManager(this.sessionHolder.get()).mapper(SchemaVersion.class);

    }

    public void createSchemaVersionTable() {
        sessionHolder.get().execute(String.format(tableCreationQuery, sessionHolder.get().getLoggedKeyspace(), SchemaConstants.TABLE_SCHEMA_VERSION));
    }

    public List<SchemaVersion> findAll() {
        Select select = QueryBuilder.select().from(SchemaConstants.TABLE_SCHEMA_VERSION);
        return schemaVersionMapper.map(sessionHolder.get().execute(select)).all();
    }

    public SchemaVersion findLastVersion() {
        Select select = QueryBuilder.select().from(SchemaConstants.TABLE_SCHEMA_VERSION);
        select.where(eq("key", "state"));
        return schemaVersionMapper.map(sessionHolder.get().execute(select)).one();
    }

    public List<SchemaVersion> getHashes() {
        Select select = QueryBuilder.select().from(SchemaConstants.TABLE_SCHEMA_VERSION);
        select.where(QueryBuilder.eq("column1", "hash"));
        select.allowFiltering();
        return schemaVersionMapper.map(sessionHolder.get().execute(select)).all();
    }

    public void add(String key, String column, ByteBuffer value) {
        SchemaVersion schemaVersion = new SchemaVersion();
        schemaVersion.setKey(key);
        schemaVersion.setColumn1(column);
        schemaVersion.setValue(value);

        schemaVersionMapper.save(schemaVersion);
    }
}
