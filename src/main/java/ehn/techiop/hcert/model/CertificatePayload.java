package ehn.techiop.hcert.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CertificatePayload {

    @JsonProperty("1")
    private String iss;

    @JsonProperty("6")
    private long iat;

    @JsonProperty("4")
    private long exp;

    @JsonProperty("-260")
    private HealthCertificate healthCertificate;


    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public HealthCertificate getHcert() {
        return healthCertificate;
    }

    public void setHcert(HealthCertificate healthCertificate) {
        this.healthCertificate = healthCertificate;
    }

}
