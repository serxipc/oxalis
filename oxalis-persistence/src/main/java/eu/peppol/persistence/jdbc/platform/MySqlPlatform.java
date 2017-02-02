/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.persistence.jdbc.platform;

/**
 * @author steinar
 *         Date: 06.11.2016
 *         Time: 17.30
 */
class MySqlPlatform extends AbstractDbmsPlatform implements DbmsPlatform {

    public MySqlPlatform(String databaseProductName) {
        super(databaseProductName);
    }

    @Override
    public String getLimitClause(int offset, int rowCount) {
        return "LIMIT " + rowCount + " OFFSET " + offset;
    }
}
