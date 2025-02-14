/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.datasource.pool.metadata.type.dbcp;

import lombok.Getter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * DBCP data source pool meta data.
 */
@Getter
public final class DBCPDataSourcePoolMetaData implements DataSourcePoolMetaData<BasicDataSource> {
    
    private final Collection<String> transientFieldNames = new LinkedList<>();
    
    public DBCPDataSourcePoolMetaData() {
        buildTransientFieldNames();
    }
    
    private void buildTransientFieldNames() {
        transientFieldNames.add("closed");
    }
    
    @Override
    public Map<String, Object> getDefaultProperties() {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, Object> getInvalidProperties() {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, String> getPropertySynonyms() {
        return Collections.emptyMap();
    }
    
    @Override
    public DBCPDataSourceJdbcUrlMetaData getJdbcUrlMetaData() {
        return new DBCPDataSourceJdbcUrlMetaData();
    }
    
    @Override
    public String getType() {
        return "org.apache.commons.dbcp2.BasicDataSource";
    }
}
