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

import com.google.inject.Inject;
import eu.peppol.persistence.guice.TestModuleFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 06.11.2016
 *         Time: 17.48
 */

@Guice(moduleFactory = TestModuleFactory.class)
public class DbmsPlatformFactoryTest {


    @Inject
    DataSource dataSource;

    @Test
    public void testPlatformFor() throws Exception {
        assertNotNull(dataSource);
        Connection connection = dataSource.getConnection();
        DbmsPlatform dbmsPlatform = DbmsPlatformFactory.platformFor(connection);
        assertNotNull(dbmsPlatform);

        String databaseProductName = dataSource.getConnection().getMetaData().getDatabaseProductName();
        assertTrue(databaseProductName.toLowerCase().contains("h2") && dbmsPlatform instanceof H2DatabasePlatform);
    }

}
