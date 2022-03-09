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

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLType;
import lombok.Getter;

@Getter
public abstract class LiaonanzhouBaseSQLStatement {
    private final String sql;

    private final String databaseType;

    private final SQLType sqlType;

    private final SQLStatement sqlStatement;

    LiaonanzhouBaseSQLStatement(final String sql, final String databaseType) {
        this.sql = sql;
        this.databaseType = databaseType;
        this.sqlStatement = SQLUtils.parseSingleMysqlStatement(this.sql);

        if (databaseType != null && databaseType.toLowerCase().equals("mysql")) {
            // 方便以后扩展，这里加了if 判断
            sqlType = SQLParserUtils.getSQLType(sql, DbType.mysql);
        } else {
            // default is mysql
            sqlType = SQLParserUtils.getSQLType(sql, DbType.mysql);
        }
    }

    @Override
    public String toString() {
        return this.getClass().toString();
    }
}
