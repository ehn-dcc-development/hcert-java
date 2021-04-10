package ehn.techiop.hcert;

import com.fasterxml.jackson.annotation.JsonProperty;
import ehn.techiop.hcert.schema.EuHcertV1Schema;

public class CWT {

    @JsonProperty("hcert")
    private EuHcertV1Schema euHcertV1Schema;

    @JsonProperty("iss")
    private String iss;

    @JsonProperty("iat")
    private long iat;

    @JsonProperty("exp")
    private long exp;

    public EuHcertV1Schema getEuHcertV1Schema() {
        return euHcertV1Schema;
    }

    public void setEuHcertV1Schema(EuHcertV1Schema euHcertV1Schema) {
        this.euHcertV1Schema = euHcertV1Schema;
    }

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
}
