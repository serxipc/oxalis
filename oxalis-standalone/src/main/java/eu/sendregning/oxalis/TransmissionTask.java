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

package eu.sendregning.oxalis;

import brave.Span;
import brave.Tracer;
import com.google.common.io.ByteStreams;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.oxalis.commons.filesystem.FileUtils;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import no.difi.vefa.peppol.common.model.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author steinar
 *         Date: 07.01.2017
 *         Time: 22.43
 */
public class TransmissionTask implements Callable<TransmissionResult> {

    public static final Logger log = LoggerFactory.getLogger(TransmissionTask.class);

    private final TransmissionParameters params;

    private final File xmlPayloadFile;

    private final Tracer tracer;

    public TransmissionTask(TransmissionParameters params, File xmlPayloadFile) {
        this.params = params;
        this.xmlPayloadFile = xmlPayloadFile;

        log.debug(xmlPayloadFile + ": " + params.toString());
        
        this.tracer = params.getOxalisOutboundComponent().getInjector().getInstance(Tracer.class);
    }

    @Override
    public TransmissionResult call() throws Exception {
        Span span = tracer.newTrace().name("standalone").start();
        try {
            TransmissionResponse transmissionResponse;
            long duration = 0;

            if (params.isUseFactory()) {
                try (InputStream inputStream = Files.newInputStream(xmlPayloadFile.toPath())) {
                    transmissionResponse = params.getOxalisOutboundComponent()
                            .getTransmissionService()
                            .send(inputStream, span);
                }
            } else {

                TransmissionRequest transmissionRequest = createTransmissionRequest(span);

                Transmitter transmitter;
                Span span1 = tracer.newChild(span.context()).name("get transmitter").start();
                try {
                    transmitter = params.getOxalisOutboundComponent().getTransmitter();
                } finally {
                    span1.finish();
                }

                // Performs the transmission
                long start = System.nanoTime();
                transmissionResponse = performTransmission(
                        params.getEvidencePath(), transmitter, transmissionRequest, span);
                long elapsed = System.nanoTime() - start;
                duration = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS);

                return new TransmissionResult(duration, transmissionResponse.getTransmissionIdentifier());
            }
            return new TransmissionResult(duration, transmissionResponse.getTransmissionIdentifier());
        } finally {
            span.finish();
        }
    }

    protected TransmissionRequest createTransmissionRequest(Span root) throws OxalisTransmissionException, IOException {
        Span span = tracer.newChild(root.context()).name("create transmission request").start();
        try {
            // creates a transmission request builder and enables trace
            TransmissionRequestBuilder requestBuilder =
                    params.getOxalisOutboundComponent().getTransmissionRequestBuilder();

            requestBuilder.setTransmissionBuilderOverride(true);

            // add receiver participant
            if (params.getReceiver().isPresent()) {
                requestBuilder.receiver(params.getReceiver().get());
            }

            // add sender participant
            if (params.getSender().isPresent()) {
                requestBuilder.sender(params.getSender().get());
            }

            if (params.getDocType().isPresent()) {
                requestBuilder.documentType(params.getDocType().get());
            }

            if (params.getProcessIdentifier().isPresent()) {
                requestBuilder.processType(params.getProcessIdentifier().get());
            }

            // Supplies the payload
            try (InputStream inputStream = new FileInputStream(xmlPayloadFile)) {
                requestBuilder.payLoad(inputStream);
            }

            // Overrides the destination URL if so requested
            if (params.getEndpoint().isPresent()) {
                final Endpoint endpoint = params.getEndpoint().get();
                requestBuilder.overrideAs2Endpoint(endpoint);
            }

            // Specifying the details completed, creates the transmission request
            return requestBuilder.build(span);
        } catch (Exception e) {
            span.tag("exception", String.valueOf(e.getMessage()));
            System.out.println("");
            System.out.println("Message failed : " + e.getMessage());
            //e.printStackTrace();
            System.out.println("");
            return null;
        } finally {
            span.finish();
        }
    }

    protected TransmissionResponse performTransmission(File evidencePath, Transmitter transmitter,
                                                       TransmissionRequest transmissionRequest, Span root)
            throws OxalisTransmissionException, IOException {
        Span span = tracer.newChild(root.context()).name("transmission").start();
        try {
            // ... and performs the transmission
            long start = System.nanoTime();
            TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest, span);
            long elapsed = System.nanoTime() - start;

            long durartionInMs = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
            // Write the transmission id and where the message was delivered
            log.debug(String.format("Message using messageId %s sent to %s using %s was assigned transmissionId %s took %dms\n",
                    transmissionResponse.getHeader().getIdentifier().getIdentifier(),
                    transmissionResponse.getEndpoint().getAddress(),
                    transmissionResponse.getProtocol().getIdentifier(),
                    transmissionResponse.getTransmissionIdentifier(),
                    durartionInMs
            ));

            saveEvidence(transmissionResponse, evidencePath, span);

            return transmissionResponse;
        } finally {
            span.finish();
        }
    }

    protected void saveEvidence(TransmissionResponse transmissionResponse, File evidencePath, Span root)
            throws IOException {
        Span span = tracer.newChild(root.context()).name("save evidence").start();
        try {
            // saveEvidence(transmissionResponse, "-rem-evidence.xml",
            // transmissionResponse::getRemEvidenceBytes, evidencePath);
            saveEvidence(transmissionResponse, "-as2-mdn.txt",
                    transmissionResponse::getNativeEvidenceBytes, evidencePath);
        } finally {
            span.finish();
        }
    }

    void saveEvidence(TransmissionResponse transmissionResponse, String suffix, Supplier<byte[]> supplier,
                      File evidencePath) throws IOException {
        String fileName = FileUtils.filterString(transmissionResponse.getTransmissionIdentifier().toString()) + suffix;
        File evidenceFile = new File(evidencePath, fileName);

        ByteStreams.copy(new ByteArrayInputStream(supplier.get()), new FileOutputStream(evidenceFile));
        log.info("Evidence written to '{}'.", evidenceFile);
    }
}
