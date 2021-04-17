package ehn.techiop.hcert.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import ehn.techiop.hcert.DigitalGreenCertificate;

public class HealthCertificate {

    @JsonProperty("1")
    private DigitalGreenCertificate digitalGreenCertificate;

    public DigitalGreenCertificate getDigitalGreenCertificate() {
        return digitalGreenCertificate;
    }

    public void setDigitalGreenCertificate(DigitalGreenCertificate digitalGreenCertificate) {
        this.digitalGreenCertificate = digitalGreenCertificate;
    }
}
