package com.chainpass.did.dto;

import jakarta.validation.constraints.NotBlank;

/** Request to verify that a challenge was signed by the DID key. */
public class VerifyDIDRequest {
    @NotBlank(message = "DID不能为空")
    private String did;

    @NotBlank(message = "挑战字符串不能为空")
    private String challenge;

    @NotBlank(message = "签名不能为空")
    private String signature;

    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }
    public String getChallenge() { return challenge; }
    public void setChallenge(String challenge) { this.challenge = challenge; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
