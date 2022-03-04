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

package org.apache.shardingsphere.driver.statement;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * sql statement builder for liaonanzhou.
 */
public class LiaonanzhouSQLStatementBuilder {

    /**
     * determine choose which type SQLStatement.
     *
     * @param sql          sql
     * @param databaseType db type
     * @return SQLStatement
     */
    public static SQLStatement builder(final String sql, final String databaseType) {
        DatabaseType actualDatabaseType = DatabaseTypeRegistry.getActualDatabaseType(databaseType);
        if (actualDatabaseType instanceof MySQLDatabaseType) {
            return new LiaonanzhouMySQLStatement(sql, databaseType);
        }
        return new LiaonanzhouMySQLStatement(sql, databaseType);
    }

}
