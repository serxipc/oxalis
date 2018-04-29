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

package no.difi.oxalis.ext.example;

import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.transmission.TransmissionVerifier;
import no.difi.oxalis.api.util.Type;
import no.difi.vefa.peppol.common.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * This is a simple implementation of {@link TransmissionVerifier} where each message is logged.
 *
 * @author erlend
 * @since 4.0.1
 */
@Singleton
@Type("logging") // Name given to the implementation for use in configuration.
public class LoggingTransmissionVerifier implements TransmissionVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingTransmissionVerifier.class);

    @Override
    public void verify(Header header, Direction direction) {
        LOGGER.info("Direction: {} | Sender/Receiver: {}/{} | Instance identifier: {}",
                direction,
                header.getSender().getIdentifier(),
                header.getReceiver().getIdentifier(),
                header.getIdentifier());
    }
}
