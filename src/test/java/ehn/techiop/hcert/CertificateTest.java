package ehn.techiop.hcert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import COSE.AlgorithmID;
import COSE.CoseException;
import COSE.OneKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import ehn.techiop.hcert.schema.EuHcertV1Schema;
import ehn.techiop.hcert.schema.Sub;
import ehn.techiop.hcert.schema.Tst;
import ehn.techiop.hcert.schema.Vac;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.compress.compressors.CompressorException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CertificateTest {

    static OneKey cborPublicKey;
    static OneKey cborPrivateKey;
    static String json;

    @BeforeAll
    static void beforeAll() throws Exception {

        cborPrivateKey = OneKey.generateKey(AlgorithmID.ECDSA_256);
        cborPublicKey = cborPrivateKey.PublicKey();

        List<Tst> tests = new ArrayList<>();
        List<Vac> vacs = new ArrayList<>();

        tests.add(new Tst()
                .withDis("Covid-19")
                .withTyp("LP6464-4")
                .withTna("Nucleic acid amplification with probe detection")
                .withTma("BIOSYNEX SWISS SA BIOSYNEX COVID-19 Ag BSS")
                .withOri("258500001")
                .withDat("2021-02-20")
                .withRes(false)
                .withFac("Region Midtjylland")
                .withCou("DNK"));
        vacs.add(new Vac()
                .withDes("J07BX03")
                .withNam("COMIRNATY")
                .withAut("Pfizer BioNTech")
                .withSeq(1)
                .withTot(2)
                .withDat("2021-02-20")
                .withAdm("Region Halland"));

        EuHcertV1Schema euvac = new EuHcertV1Schema()
                .withSub(new Sub().withN("Gaby Doe"))
                .withTst(tests)
                .withVac(vacs);

        CWT cwt = new CWT();
        Hcert hcert = new Hcert();
        hcert.setEuHcertV1Schema(euvac);
        cwt.setHcert(hcert);
        cwt.setExp(new Date().getTime() / 1000);
        cwt.setIat(new Date().getTime() / 1000);
        cwt.setIss("DNK");
        json = new ObjectMapper().writeValueAsString(cwt);
    }

    @Test
    void roundtrip() throws CompressorException, CoseException, IOException {

        String encoded = new GreenCertificateEncoder(cborPrivateKey, UUID.randomUUID().toString()).encode(json);
        String result = new GreenCertificateDecoder(cborPublicKey).decode(encoded);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(json), mapper.readTree(result));
    }


    @Test
    void wrongPublicKey() throws CompressorException, CoseException, IOException {

        OneKey anotherPublicKey = OneKey.generateKey(AlgorithmID.ECDSA_256).PublicKey();

        String encoded = new GreenCertificateEncoder(cborPrivateKey, UUID.randomUUID().toString()).encode(json);

        Exception exception = assertThrows(RuntimeException.class,
                () -> new GreenCertificateDecoder(anotherPublicKey).decode(encoded));
        assertEquals("Could not verify COSE signature", exception.getMessage());

    }

    @Test
    void rsaKeys() throws CompressorException, CoseException, IOException, NoSuchAlgorithmException {

        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.genKeyPair();
        OneKey keys = OneKey.generateKey(AlgorithmID.RSA_PSS_256);

        String encoded = new GreenCertificateEncoder(keys, UUID.randomUUID().toString()).encode(json);
        String result = new GreenCertificateDecoder(keys).decode(encoded);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(json), mapper.readTree(result));
    }
}
