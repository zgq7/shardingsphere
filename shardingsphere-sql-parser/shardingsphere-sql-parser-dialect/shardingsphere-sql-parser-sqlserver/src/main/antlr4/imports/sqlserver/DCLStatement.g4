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

grammar DCLStatement;

import BaseRule;

grant
    : GRANT (classPrivilegesClause | classTypePrivilegesClause | roleClause)
    ;

revoke
    : REVOKE (optionForClause? classPrivilegesClause | classTypePrivilegesClause | roleClause)
    ;

deny
    : DENY (classPrivilegesClause | classTypePrivilegesClause)
    ;

classPrivilegesClause
    : classPrivileges (ON onClassClause)?
    ;

classTypePrivilegesClause
    : classTypePrivileges (ON onClassTypeClause)?
    ;

optionForClause
    : GRANT OPTION FOR
    ;

classPrivileges
    : privilegeType columnNames? (COMMA_ privilegeType columnNames?)*
    ;

onClassClause
    : class_? tableName
    ;

classTypePrivileges
    : privilegeType (COMMA_ privilegeType)*
    ;

onClassTypeClause
    : classType? tableName
    ;

privilegeType
    : ALL PRIVILEGES?
    | basicPermission | objectPermission
    | serverPermission | serverPrincipalPermission
    | databasePermission | databasePrincipalPermission | schemaPermission
    | serviceBrokerPermission | endpointPermission
    | certificatePermission | symmetricKeyPermission | asymmetricKeyPermission
    | assemblyPermission | availabilityGroupPermission | fullTextPermission
    ;

basicPermission
    : CONTROL SERVER? | TAKE OWNERSHIP | ALTER | VIEW ANY? DEFINITION | REFERENCES
    | SELECT | INSERT | UPDATE | DELETE | EXECUTE | RECEIVE
    ;

objectPermission
    : VIEW CHANGE TRACKING
    ;

serverPermission
    : ALTER (RESOURCES | SETTINGS | TRACE | SERVER STATE)
    | ALTER ANY (AVAILABILITY GROUP | CONNECTION | CREDENTIAL | DATABASE | ENDPOINT | EVENT NOTIFICATION | EVENT SESSION | LINKED SERVER | LOGIN | SERVER AUDIT | SERVER ROLE)
    | CREATE (AVAILABILITY GROUP | DDL EVENT NOTIFICATION | ENDPOINT | SERVER ROLE | TRACE EVENT NOTIFICATION)
    | CREATE ANY DATABASE
    | VIEW SERVER STATE
    | VIEW ANY (DATABASE | DEFINITION)
    | CONNECT ANY DATABASE | CONNECT SQL
    | IMPERSONATE ANY LOGIN
    | SELECT ALL USER SECURABLES | AUTHENTICATE SERVER | EXTERNAL ACCESS ASSEMBLY | ADMINISTER BULK OPERATIONS | UNSAFE ASSEMBLY
    | SHUTDOWN
    ;

serverPrincipalPermission
    : IMPERSONATE | ALTER ANY (LOGIN | SERVER ROLE)
    ;

databasePermission
    : ALTER TRACE
    | ALTER ANY (DATABASE (AUDIT | DDL TRIGGER | EVENT NOTIFICATION | EVENT SESSION | SCOPED CONFIGURATION)? | DATASPACE | SCHEMA
      | SERVICE AUDIT?| USER | APPLICATION? ROLE | CERTIFICATE | CONTRACT | ASSEMBLY | CONNECTION
      | (SYMMETRIC | ASYMMETRIC | COLUMN ENCRYPTION) KEY | COLUMN MASTER KEY DEFINITION
      | EXTERNAL (DATA SOURCE | FILE FORMAT | LIBRARY)
      | FULLTEXT CATALOG | MASK | MESSAGE TYPE | REMOTE SERVICE BINDING | ROUTE | EVENT SESSION | SECURITY POLICY)
    | CREATE (DATABASE | DATABASE DDL EVENT NOTIFICATION | SCHEMA | TABLE | VIEW | SERVICE | TYPE | DEFAULT | AGGREGATE | ASSEMBLY | (SYMMETRIC | ASYMMETRIC) KEY 
      | CERTIFICATE | CONTRACT | FULLTEXT CATALOG | FUNCTION | MESSAGE TYPE | PROCEDURE | QUEUE | REMOTE SERVICE BINDING | ROLE | ROUTE | RULE | SYNONYM | XML SCHEMA COLLECTION)
    | CREATE ANY (DATABASE | EXTERNAL LIBRARY)
    | VIEW ((DATABASE | SERVER) STATE | DDL EVENT NOTIFICATION)
    | VIEW ANY (COLUMN (MASTER | ENCRYPTION) KEY DEFINITION | DEFINITION)
    | EXECUTE ANY EXTERNAL SCRIPT | CONNECT REPLICATION? | KILL DATABASE CONNECTION
    | BACKUP (DATABASE | LOG) 
    | AUTHENTICATE SERVER? | SHOWPLAN | SUBSCRIBE QUERY NOTIFICATIONS | UNMASK | CHECKPOINT | ADMINISTER DATABASE BULK OPERATIONS
    ;

databasePrincipalPermission
    : IMPERSONATE | ALTER ANY (USER | APPLICATION? ROLE)
    ;

schemaPermission
    : ALTER ANY SCHEMA | CREATE SEQUENCE | VIEW CHANGE TRACKING
    ;

serviceBrokerPermission
    : ALTER ANY (CONTRACT | MESSAGE TYPE | REMOTE SERVICE BINDING | ROUTE | SERVICE)
    ;

endpointPermission
    : ALTER ANY ENDPOINT
    ;

certificatePermission
    : ALTER ANY CERTIFICATE
    ;

symmetricKeyPermission
    : ALTER ANY SYMMETRIC KEY
    ;

asymmetricKeyPermission
    : ALTER ANY ASYMMETRIC KEY
    ;

assemblyPermission
    : ALTER ANY ASSEMBLY
    ;

availabilityGroupPermission
    : ALTER ANY AVAILABILITY GROUP | CONNECT
    ;

fullTextPermission
    : ALTER ANY FULLTEXT CATALOG
    ;

class_
    : IDENTIFIER_ COLON_ COLON_
    ;

classType
    : (LOGIN | DATABASE | OBJECT | ROLE | SCHEMA | USER) COLON_ COLON_
    ;

roleClause
    : ignoredIdentifiers
    ;

setUser
    : SETUSER (stringLiterals (WITH NORESET)?)?
    ;

createUser
    : CREATE USER
    (createUserLoginClause 
    | createUserWindowsPrincipalClause 
    | createUserLoginWindowsPrincipalClause
    | createUserWithoutLoginClause
    | createUserFromExternalProviderClause
    | createUserWithDefaultSchema
    | createUserWithAzureActiveDirectoryPrincipalClause
    | userName)?
    ;

createUserLoginClause
    : userName ((FOR | FROM) LOGIN identifier)? (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    ;

createUserWindowsPrincipalClause
    : windowsPrincipal (WITH optionsList (COMMA_ optionsList)*)?
    | userName WITH PASSWORD EQ_ stringLiterals (COMMA_ optionsList (COMMA_ optionsList)*)?
    | azureActiveDirectoryPrincipal FROM EXTERNAL PROVIDER
    ;

createUserLoginWindowsPrincipalClause
    : ((windowsPrincipal ((FOR | FROM) LOGIN windowsPrincipal)?) | (userName (FOR | FROM) LOGIN windowsPrincipal))
    (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    ;

createUserWithoutLoginClause
    : userName (WITHOUT LOGIN (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    | (FOR | FROM) CERTIFICATE identifier
    | (FOR | FROM) ASYMMETRIC KEY identifier)
    ;

optionsList
    : DEFAULT_SCHEMA EQ_ schemaName
    | DEFAULT_LANGUAGE EQ_ (NONE | identifier)
    | SID EQ_ (NCHAR_TEXT | HEX_DIGIT_)
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)?
    ;

limitedOptionsList
    : DEFAULT_SCHEMA EQ_ schemaName
    | DEFAULT_LANGUAGE EQ_ (NONE | identifier)
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)?
    ;

createUserFromExternalProviderClause
    : userName ((FOR | FROM) LOGIN identifier)? | FROM EXTERNAL PROVIDER (WITH limitedOptionsList (COMMA_ limitedOptionsList)*)?
    ;

createUserWithDefaultSchema
    : userName ((FOR | FROM) LOGIN identifier | WITHOUT LOGIN)? (WITH DEFAULT_SCHEMA EQ_ schemaName)?
    ;

createUserWithAzureActiveDirectoryPrincipalClause
    : azureActiveDirectoryPrincipal FROM EXTERNAL PROVIDER (WITH DEFAULT_SCHEMA EQ_ schemaName)?
    ;

windowsPrincipal
    : userName
    ;

azureActiveDirectoryPrincipal
    : userName
    ;

userName
    : ignoredNameIdentifier
    ;

ignoredNameIdentifier
    : identifier (DOT_ identifier)?
    ;

dropUser
    : DROP USER
    ;

alterUser
    : ALTER USER userName (WITH setItem (COMMA_ setItem)* | FROM EXTERNAL PROVIDER)
    ;

setItem
    : NAME EQ_ userName
    | DEFAULT_SCHEMA EQ_ (schemaName | NULL)
    | LOGIN EQ_ identifier
    | PASSWORD EQ_ stringLiterals (OLD_PASSWORD EQ_ stringLiterals)?
    | DEFAULT_LANGUAGE EQ_ (NONE | identifier)
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)?
    ;

createRole
    : CREATE ROLE
    ;

dropRole
    : DROP ROLE
    ;

alterRole
    : ALTER ROLE
    ;

createLogin
    : CREATE LOGIN
    ;

dropLogin
    : DROP LOGIN
    ;

alterLogin
    : ALTER LOGIN
    ;
