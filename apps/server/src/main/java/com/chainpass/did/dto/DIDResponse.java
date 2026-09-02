package com.chainpass.did.dto;

import com.chainpass.did.entity.DIDDocument;

/** Public projection of a locally registered DID. */
public class DIDResponse {
    private String did;
    private DIDDocument document;
    private String publicKey;
    private String status;
    private String createdAt;
    private String expiresAt;

    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }
    public DIDDocument getDocument() { return document; }
    public void setDocument(DIDDocument document) { this.document = document; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
