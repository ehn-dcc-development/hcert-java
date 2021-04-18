package ehn.techiop.hcert;

import COSE.AlgorithmID;
import COSE.CoseException;
import COSE.OneKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ehn.techiop.hcert.model.CertificatePayload;
import ehn.techiop.hcert.model.HealthCertificate;
import org.apache.commons.compress.compressors.CompressorException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.service.impl.DefaultDGCEncoder;
import se.digg.dgc.signatures.impl.DefaultDGCSigner;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

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


        List<Vac> vacs = new ArrayList<>();
        List<Tst> tests = new ArrayList<>();
        Tst test = new Tst()
                .withDis("840539006")
                .withTyp("LP6464-4")
                .withTna("Nucleic acid amplification with probe detection")
                .withTma("BIOSYNEX SWISS SA BIOSYNEX COVID-19 Ag BSS")
                .withOri("258500001")
                .withDtr(Math.toIntExact(LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)))
                .withDts(Math.toIntExact(LocalDateTime.now().minusHours(4).toEpochSecond(ZoneOffset.UTC)))
                .withRes("1240591000000102")
                .withFac("Region Midtjylland")
                .withCou("DNK");
        vacs.add(new Vac()
                .withDis("840539006")
                .withAut("Pfizer BioNTech")
                .withSeq(1)
                .withTot(2)
                .withDat("2021-02-20")
                .withAdm("Region Halland"));

        tests.add(test);

        DigitalGreenCertificate euvac = new DigitalGreenCertificate()
                .withSub(new Sub().withGn("Gaby").withFn("Doe"))
                .withTst(tests)
                .withVac(vacs);


        CertificatePayload certificatePayload = new CertificatePayload();
        HealthCertificate healthCertificate = new HealthCertificate();
        healthCertificate.setDigitalGreenCertificate(euvac);
        certificatePayload.setHcert(healthCertificate);
        certificatePayload.setExp(new Date().getTime() / 1000);
        certificatePayload.setIat(new Date().getTime() / 1000);
        certificatePayload.setIss("DNK");
        json = new ObjectMapper().writeValueAsString(certificatePayload);
    }

    @Test
    void roundtrip() throws CompressorException, CoseException, IOException {

        String encoded = new GreenCertificateEncoder(cborPrivateKey, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)).encode(json);
        String result = new GreenCertificateDecoder((kid, issuer) -> cborPublicKey.AsPublicKey()).decode(encoded);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(json), mapper.readTree(result));
    }


    @Test
    void wrongPublicKey() throws CompressorException, CoseException, IOException {

        OneKey anotherPublicKey = OneKey.generateKey(AlgorithmID.ECDSA_256).PublicKey();

        String encoded = new GreenCertificateEncoder(cborPrivateKey, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)).encode(json);

        Exception exception = assertThrows(RuntimeException.class,
                () -> new GreenCertificateDecoder((kid, issuer) -> anotherPublicKey.AsPublicKey()).decode(encoded));
        assertEquals("Could not verify COSE signature", exception.getMessage());

    }

    @Test
    void rsaKeys() throws CompressorException, CoseException, IOException, NoSuchAlgorithmException {

        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        OneKey keys = OneKey.generateKey(AlgorithmID.RSA_PSS_256);

        String encoded = new GreenCertificateEncoder(keys, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)).encode(json);
        String result = new GreenCertificateDecoder((kid, issuer) -> keys.AsPublicKey()).decode(encoded);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(json), mapper.readTree(result));
    }

    CertificatePayload test_0405870101 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withVaccine()
            .withTestResult()
            .withRecovery()
            .build();

    CertificatePayload test_0405870102 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withVaccine()
            .withTestResult()
            .withExpiredRecovery()
            .build();

    CertificatePayload test_0405870103 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withVaccine()
            .withExpiredTestResult()
            .withExpiredRecovery()
            .build();

    CertificatePayload test_0405870104 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withExpiredVaccine()
            .withExpiredTestResult()
            .withExpiredRecovery()
            .build();

    CertificatePayload test_0405870105 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withExpiredVaccine()
            .withTestResult()
            .withExpiredRecovery()
            .build();

    CertificatePayload test_0405870106 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withExpiredVaccine()
            .withTestResult()
            .withRecovery()
            .build();

    CertificatePayload test_0405870107 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withVaccine()
            .withExpiredTestResult()
            .withRecovery()
            .build();

    CertificatePayload test_0405870108 = new CertificateDSL()
            .withSubject("Judy", "Jensen")
            .withExpiredVaccine()
            .withExpiredTestResult()
            .withRecovery()
            .build();

    CertificatePayload test_0405870109 = new CertificateDSL()
            .withSubject("Øjvind", "Ørn")
            .withExpiredVaccine()
            .withExpiredTestResult()
            .withRecovery()
            .expiredBuild();

    @Test
    void testSetDKExample() throws IOException, CompressorException, CoseException {

        String input = new ObjectMapper().writeValueAsString(test_0405870101);

        String encoded = new GreenCertificateEncoder(cborPrivateKey, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)).encode(input);
        String result = new GreenCertificateDecoder((kid, issuer) -> cborPublicKey.AsPublicKey()).decode(encoded);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(input), mapper.readTree(result));
    }

    @Test
    void testAgainstDGCJava() throws CertificateException, IOException, NoSuchAlgorithmException, DGCSchemaException, SignatureException, CompressorException, CoseException, NoSuchProviderException, InvalidKeyException {

        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Certificate certificate = generateCertificate(keyPair);


        se.digg.dgc.payload.v1.DigitalGreenCertificate testData = DefaultDGCExample.getTestDGC();
        DefaultDGCEncoder encoder = new DefaultDGCEncoder(new DefaultDGCSigner(keyPair.getPrivate(), (X509Certificate) certificate));
        String bytes = encoder.encode(testData, LocalDateTime.now().toInstant(ZoneOffset.UTC));
        String result = new GreenCertificateDecoder((kid, issuer) -> certificate.getPublicKey()).decode(bytes);


        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


        DigitalGreenCertificate dgcResult = mapper.readValue(result, CertificatePayload.class).getHcert().getDigitalGreenCertificate();
        assertEquals( mapper.valueToTree(testData), mapper.valueToTree(dgcResult));
    }



    public static Certificate generateCertificate(KeyPair keyPair) throws CertificateException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        // yesterday
        Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        // in 2 years
        Date validityEndDate = new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000);

        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal dnName = new X500Principal("CN=John Doe, C=DK");
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(dnName);
        certGen.setIssuerDN(dnName); // use the same
        certGen.setNotBefore(validityBeginDate);
        certGen.setNotAfter(validityEndDate);
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        return certGen.generate(keyPair.getPrivate(), "BC");

    }
}
