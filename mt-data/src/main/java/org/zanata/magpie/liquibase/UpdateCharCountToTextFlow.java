/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie.liquibase;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.zanata.magpie.util.CountUtil;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class UpdateCharCountToTextFlow implements CustomTaskChange {

    @Override
    public String getConfirmationMessage() {
        return "UpdateCharCountToTextFlow updated charCount column in TextFlow table";
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor accessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection conn = (JdbcConnection) database.getConnection();

        try (Statement stmt =
                conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE)) {

            long totalRows;
            String countSql =
                    "select count(*) from TextFlow where charCount = 0";
            try (ResultSet resultSet = stmt.executeQuery(countSql)) {
                resultSet.next();
                totalRows = resultSet.getLong(1);
            }

            Logger log = LogFactory.getInstance().getLog();
            log.info("UpdateCharCountToTextFlow: updating " + totalRows + " rows");

            String textFlowSql =
                    "select tf.content, tf.charCount, loc.localeCode from TextFlow tf join Locale loc on tf.localeId = loc.id where tf.charCount = 0";
            try (ResultSet resultSet = stmt.executeQuery(textFlowSql)) {
                long rowsUpdated = 0;
                while (resultSet.next()) {
                    String content = resultSet.getString(1);
                    String locale = resultSet.getString(3);
                    long charCount = CountUtil.countCharacters(content);
                    resultSet.updateLong(2, charCount);
                    resultSet.updateRow();
                    if (++rowsUpdated % 10000 == 0) {
                        log.info("CountWordsInHTextFlow: updated "
                                + rowsUpdated + "/" + totalRows);
                    }
                }
            }
            log.info("UpdateCharCountToTextFlow: finished");
        } catch (SQLException | DatabaseException e) {
            throw new CustomChangeException(e);
        }
    }

}

