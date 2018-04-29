/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package no.difi.oxalis.statistics.inbound;

import com.google.inject.Injector;
import no.difi.oxalis.inbound.OxalisGuiceContextListener;
import no.difi.oxalis.test.jetty.AbstractJettyServerTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;

@Test
public class StatisticsServletTest extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return new OxalisGuiceContextListener().getInjector();
    }

    @Test
    public void emptyGet() throws Exception {
        HttpURLConnection httpURLConnection =
                (HttpURLConnection) new URL("http://localhost:8080/statistics/").openConnection();

        Assert.assertEquals(httpURLConnection.getResponseCode(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void post() throws Exception {
        HttpURLConnection httpURLConnection =
                (HttpURLConnection) new URL("http://localhost:8080/statistics/").openConnection();
        httpURLConnection.setRequestMethod("POST");

        Assert.assertEquals(httpURLConnection.getResponseCode(), HttpServletResponse.SC_OK);
    }
}
