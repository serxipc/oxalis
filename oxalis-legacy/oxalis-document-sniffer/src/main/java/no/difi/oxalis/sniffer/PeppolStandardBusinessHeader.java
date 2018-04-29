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

package no.difi.oxalis.sniffer;

import no.difi.oxalis.sniffer.identifier.InstanceId;
import no.difi.oxalis.sniffer.identifier.PeppolDocumentTypeId;
import no.difi.vefa.peppol.common.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Our representation of the SBDH (Standard Business Document Header), which makes us
 * independent of the StandardBusinessDocumentHeader generated by JAXB. Furthermore
 * the UN/CEFACT SBDH is kind of awkward to use as some of the elements of interest to us,
 * are split into several fields.
 *
 * @author steinar
 * @author thore
 */
public class PeppolStandardBusinessHeader {

    /**
     * Peppol Participant Identification for the recipient
     */
    private ParticipantIdentifier recipientId;

    /**
     * Peppol Participant Identification for the sender
     */
    private ParticipantIdentifier senderId;

    /**
     * The type of document to send
     */
    private DocumentTypeIdentifier peppolDocumentTypeId;

    /**
     * The business process this document is a part of
     */
    private ProcessIdentifier profileTypeIdentifier;

    /**
     * Represents the unique identity of the message envelope. It is not the same as the ID of the
     * business message (such as the Invoice number). Nor is it the same as the transmission Message ID
     * generated by the application sending the message (as defined in AS2).
     * <p>
     * This messageId is not the same as the "AS2 Message-ID" or the "START message id", which really are
     * unique "transmission id's" that should be unique for each transmission.
     * <p>
     * <code>//StandardBusinessDocumentHeader/DocumentIdentification/InstanceIdentifier</code>
     */
    private InstanceId instanceId;

    private Date creationDateAndTime;

    /**
     * Set the time to current and makes a random TransmissionIdentifier as default
     */
    public static PeppolStandardBusinessHeader createPeppolStandardBusinessHeaderWithNewDate() {
        PeppolStandardBusinessHeader p = new PeppolStandardBusinessHeader();
        p.setCreationDateAndTime(new Date());
        return p;
    }

    /**
     * Empty constructor, no defaults - all must be supplied by user
     */
    public PeppolStandardBusinessHeader() {
        /* intentionally nothing */
    }

    public PeppolStandardBusinessHeader(Header header) {
        senderId = header.getSender();
        recipientId = header.getReceiver();
        creationDateAndTime = header.getCreationTimestamp();
        peppolDocumentTypeId = header.getDocumentType();
        profileTypeIdentifier = header.getProcess();
        instanceId = new InstanceId(header.getIdentifier().getIdentifier());
    }

    /**
     * Copy constructor
     */
    public PeppolStandardBusinessHeader(PeppolStandardBusinessHeader peppolStandardBusinessHeader) {
        recipientId = peppolStandardBusinessHeader.getRecipientId();
        senderId = peppolStandardBusinessHeader.getSenderId();
        peppolDocumentTypeId = peppolStandardBusinessHeader.getDocumentTypeIdentifier();
        profileTypeIdentifier = peppolStandardBusinessHeader.getProfileTypeIdentifier();
        instanceId = peppolStandardBusinessHeader.getInstanceId();
        creationDateAndTime = peppolStandardBusinessHeader.getCreationDateAndTime();
    }

    /**
     * Do we have enough transport details to send the message?
     *
     * @return true if transport details are complete.
     */
    public boolean isComplete() {
        return ((recipientId != null) &&
                (senderId != null) &&
                (peppolDocumentTypeId != null) &&
                (profileTypeIdentifier != null) &&
                (instanceId != null) &&
                (creationDateAndTime != null));
    }

    /**
     * Returns a list of property names that are still missing.
     *
     * @return empty list if headers are complete
     */
    public List<String> listMissingProperties() {
        List<String> mhf = new ArrayList<>();
        if (recipientId == null) mhf.add("recipientId");
        if (senderId == null) mhf.add("senderId");
        if (peppolDocumentTypeId == null) mhf.add("peppolDocumentTypeId");
        if (profileTypeIdentifier == null) mhf.add("profileTypeIdentifier");
        if (instanceId == null) mhf.add("messageId");
        if (creationDateAndTime == null) mhf.add("creationDateAndTime");
        return mhf;
    }

    public void setRecipientId(ParticipantIdentifier recipientId) {
        this.recipientId = recipientId;
    }

    public ParticipantIdentifier getRecipientId() {
        return recipientId;
    }

    public void setSenderId(ParticipantIdentifier senderId) {
        this.senderId = senderId;
    }

    public ParticipantIdentifier getSenderId() {
        return senderId;
    }

    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public InstanceId getInstanceId() {
        return instanceId;
    }

    public void setCreationDateAndTime(Date creationDateAndTime) {
        this.creationDateAndTime = creationDateAndTime;
    }

    public Date getCreationDateAndTime() {
        return creationDateAndTime;
    }

    @Deprecated
    public void setDocumentTypeIdentifier(PeppolDocumentTypeId documentTypeIdentifier) {
        setDocumentTypeIdentifier(documentTypeIdentifier.toVefa());
    }

    public void setDocumentTypeIdentifier(DocumentTypeIdentifier documentTypeIdentifier) {
        this.peppolDocumentTypeId = documentTypeIdentifier;
    }

    public DocumentTypeIdentifier getDocumentTypeIdentifier() {
        return peppolDocumentTypeId;
    }

    public void setProfileTypeIdentifier(ProcessIdentifier processIdentifier) {
        this.profileTypeIdentifier = processIdentifier;
    }

    public ProcessIdentifier getProfileTypeIdentifier() {
        return profileTypeIdentifier;
    }

    public Header toVefa() {
        PeppolDocumentTypeId documentTypeId = PeppolDocumentTypeId.valueOf(peppolDocumentTypeId.getIdentifier());

        return Header.of(
                senderId,
                recipientId,
                profileTypeIdentifier,
                peppolDocumentTypeId,
                instanceId == null ? InstanceIdentifier.generateUUID() : instanceId.toVefa(),
                InstanceType.of(
                        documentTypeId.getRootNameSpace(),
                        documentTypeId.getLocalName(),
                        documentTypeId.getVersion()
                ),
                creationDateAndTime
        );
    }
}
