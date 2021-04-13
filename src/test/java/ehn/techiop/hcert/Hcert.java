package ehn.techiop.hcert;

import com.fasterxml.jackson.annotation.JsonProperty;
import ehn.techiop.hcert.schema.EuHcertV1Schema;

public class Hcert {

    @JsonProperty("1")
    private EuHcertV1Schema euHcertV1Schema;

    public EuHcertV1Schema getEuHcertV1Schema() {
        return euHcertV1Schema;
    }

    public void setEuHcertV1Schema(EuHcertV1Schema euHcertV1Schema) {
        this.euHcertV1Schema = euHcertV1Schema;
    }

}
