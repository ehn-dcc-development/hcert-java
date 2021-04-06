package ehn.techiop.hcert;

import COSE.AlgorithmID;
import COSE.CoseException;
import COSE.OneKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import ehn.techiop.hcert.schema.EuHcertV1Schema;
import ehn.techiop.hcert.schema.Id;
import ehn.techiop.hcert.schema.Sub;
import ehn.techiop.hcert.schema.Tst;
import org.apache.commons.compress.compressors.CompressorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CertificateTest {

    static OneKey cborPublicKey;
    static OneKey cborPrivateKey;
    static String json;

    @BeforeAll
    static void beforeAll() throws Exception {

        cborPrivateKey = OneKey.generateKey(AlgorithmID.ECDSA_256);
        cborPublicKey = cborPrivateKey.PublicKey();

        List<Id> ids = new ArrayList<Id>();
        List<Tst> tests = new ArrayList<Tst>();

        ids.add(new Id().withI("0102030405").withT(Id.T.NID));
        tests.add(new Tst().withCou("DNK").withDat("2021-02-20").withDis("Covid-19"));
        EuHcertV1Schema euvac = new EuHcertV1Schema()
                .withSub(new Sub().withN("Gaby Doe").withId(ids))
                .withTst(tests);

        json = new ObjectMapper().writeValueAsString(euvac);
    }

    @Test
    void roundtrip() throws CompressorException, CoseException, IOException {

        String encoded = new GreenCertificateEncoder(cborPrivateKey).encode(json);
        String result = new GreenCertificateDecoder(cborPublicKey).decode(encoded);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(json), mapper.readTree(result));
    }

    @Test
    void roundtrip2() throws CompressorException, CoseException, IOException {

        String encoded = new GreenCertificateEncoder(cborPrivateKey).encode(json);
        String result = new GreenCertificateDecoder((ECPublicKey) cborPublicKey.AsPublicKey()).decode(encoded);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(json), mapper.readTree(result));
    }

    @Test
    void wrongPublicKey() throws CompressorException, CoseException, IOException {

        OneKey anotherPublicKey = OneKey.generateKey(AlgorithmID.ECDSA_256).PublicKey();

        String encoded = new GreenCertificateEncoder(cborPrivateKey).encode(json);

        Exception exception = assertThrows(RuntimeException.class,
                () -> new GreenCertificateDecoder(anotherPublicKey).decode(encoded));
        assertEquals("Could not verify COSE signature", exception.getMessage());

    }


}
