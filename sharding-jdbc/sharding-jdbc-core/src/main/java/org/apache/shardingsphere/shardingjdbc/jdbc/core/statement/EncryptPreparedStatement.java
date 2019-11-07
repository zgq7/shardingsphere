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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.preprocessor.SQLStatementContextFactory;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.core.rewrite.engine.impl.DefaultSQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.route.SQLLogger;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractShardingPreparedStatementAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.EncryptResultSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Encrypt prepared statement.
 *
 * @author panjuan
 */
public final class EncryptPreparedStatement extends AbstractShardingPreparedStatementAdapter {
    
    private final EncryptPreparedStatementGenerator preparedStatementGenerator;
    
    private PreparedStatement preparedStatement;
    
    private SQLStatementContext sqlStatementContext;
    
    private EncryptResultSet resultSet;
    
    private final String sql;
    
    private final Collection<SQLUnit> sqlUnits = new LinkedList<>();
    
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql) throws SQLException {
        this(connection, sql, -1, -1, -1, -1, null, null);
    }
    
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, -1, -1, null, null);
    }
    
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability, -1, null, null);
    }
    
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int autoGeneratedKeys) throws SQLException {
        this(connection, sql, -1, -1, -1, autoGeneratedKeys, null, null);
    }
    
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int[] columnIndexes) throws SQLException {
        this(connection, sql, -1, -1, -1, -1, columnIndexes, null);
    }
    
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final String[] columnNames) throws SQLException {
        this(connection, sql, -1, -1, -1, -1, null, columnNames);
    }
    
    public EncryptPreparedStatement(final EncryptConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability,
                                    final int autoGeneratedKeys, final int[] columnIndexes, final String[] columnNames) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        this.sql = sql;
        preparedStatementGenerator = new EncryptPreparedStatementGenerator(connection, resultSetType, resultSetConcurrency, resultSetHoldability, autoGeneratedKeys, columnIndexes, columnNames);
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            SQLUnit sqlUnit = getSQLUnit(sql);
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnit.getSql());
            replayMethodsInvocation(preparedStatement);
            replaySetParameter(preparedStatement, sqlUnit.getParameters());
            this.resultSet = new EncryptResultSet(preparedStatementGenerator.connection.getRuntimeContext(), sqlStatementContext, this, preparedStatement.executeQuery());
            return resultSet;
        } finally {
            clearParameters();
        }
    }
    
    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        try {
            SQLUnit sqlUnit = getSQLUnit(sql);
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnit.getSql());
            replayMethodsInvocation(preparedStatement);
            replaySetParameter(preparedStatement, sqlUnit.getParameters());
            return preparedStatement.executeUpdate();
        } finally {
            clearParameters();
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        try {
            SQLUnit sqlUnit = getSQLUnit(sql);
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnit.getSql());
            replayMethodsInvocation(preparedStatement);
            replaySetParameter(preparedStatement, sqlUnit.getParameters());
            boolean result = preparedStatement.execute();
            this.resultSet = createEncryptResultSet(preparedStatement);
            return result;
        } finally {
            clearParameters();
        }
    }
    
    private EncryptResultSet createEncryptResultSet(final PreparedStatement preparedStatement) throws SQLException {
        return null == preparedStatement.getResultSet() 
                ? null : new EncryptResultSet(preparedStatementGenerator.connection.getRuntimeContext(), sqlStatementContext, this, preparedStatement.getResultSet());
    }
    
    @Override
    public void addBatch() {
        sqlUnits.add(getSQLUnit(sql));
        clearParameters();
    }
    
    @SuppressWarnings("unchecked")
    private SQLUnit getSQLUnit(final String sql) {
        EncryptConnection connection = preparedStatementGenerator.connection;
        SQLStatement sqlStatement = connection.getRuntimeContext().getParseEngine().parse(sql, true);
        sqlStatementContext = SQLStatementContextFactory.newInstance(connection.getRuntimeContext().getTableMetas(), sql, getParameters(), sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(connection.getRuntimeContext().getTableMetas(), sqlStatementContext, sql, getParameters());
        boolean isQueryWithCipherColumn = connection.getRuntimeContext().getProps().<Boolean>getValue(ShardingPropertiesConstant.QUERY_WITH_CIPHER_COLUMN);
        new EncryptSQLRewriteContextDecorator(connection.getRuntimeContext().getRule(), isQueryWithCipherColumn).decorate(sqlRewriteContext);
        sqlRewriteContext.generateSQLTokens();
        SQLRewriteResult sqlRewriteResult = new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext);
        showSQL(sqlRewriteResult.getSql());
        return new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters());
    }
    
    private void showSQL(final String sql) {
        boolean showSQL = preparedStatementGenerator.connection.getRuntimeContext().getProps().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW);
        if (showSQL) {
            SQLLogger.logSQL(sql);
        }
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        try {
            preparedStatement = preparedStatementGenerator.createPreparedStatement(sqlUnits.iterator().next().getSql());
            replayMethodsInvocation(preparedStatement);
            replayBatchPreparedStatement();
            return preparedStatement.executeBatch();
        } finally {
            clearBatch();
        }
    }
    
    private void replayBatchPreparedStatement() throws SQLException {
        for (SQLUnit each : sqlUnits) {
            replaySetParameter(preparedStatement, each.getParameters());
            preparedStatement.addBatch();
        }
    }
    
    @Override
    public void clearBatch() throws SQLException {
        preparedStatement.clearBatch();
        sqlUnits.clear();
        clearParameters();
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return preparedStatement.getGeneratedKeys();
    }
    
    @Override
    public Connection getConnection() {
        return preparedStatementGenerator.connection;
    }
    
    @Override
    public int getResultSetConcurrency() {
        return preparedStatementGenerator.resultSetConcurrency;
    }
    
    @Override
    public int getResultSetType() {
        return preparedStatementGenerator.resultSetType;
    }
    
    @Override
    public int getResultSetHoldability() {
        return preparedStatementGenerator.resultSetHoldability;
    }
    
    @Override
    protected boolean isAccumulate() {
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected Collection<? extends Statement> getRoutedStatements() {
        Collection<Statement> result = new LinkedList();
        if (null == preparedStatement) {
            return result;
        }
        result.add(preparedStatement);
        return result;
    }
    
    @RequiredArgsConstructor
    private final class EncryptPreparedStatementGenerator {
        
        private final EncryptConnection connection;
        
        private final int resultSetType;
        
        private final int resultSetConcurrency;
        
        private final int resultSetHoldability;
        
        private final int autoGeneratedKeys;
        
        private final int[] columnIndexes;
        
        private final String[] columnNames;
        
        private PreparedStatement createPreparedStatement(final String sql) throws SQLException {
            if (-1 != resultSetType && -1 != resultSetConcurrency && -1 != resultSetHoldability) {
                return connection.getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            }
            if (-1 != resultSetType && -1 != resultSetConcurrency) {
                return connection.getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
            }
            if (-1 != autoGeneratedKeys) {
                return connection.getConnection().prepareStatement(sql, autoGeneratedKeys);
            }
            if (null != columnIndexes) {
                return connection.getConnection().prepareStatement(sql, columnIndexes);
            }
            if (null != columnNames) {
                return connection.getConnection().prepareStatement(sql, columnNames);
            }
            return connection.getConnection().prepareStatement(sql);
        }
    }
}
