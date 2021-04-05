package ehn.techiop.hcert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import COSE.AlgorithmID;
import COSE.CoseException;
import COSE.OneKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import org.apache.commons.compress.compressors.CompressorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CertificateTest {

  static OneKey cborPublicKey;
  static OneKey cborPrivateKey;

  String json =
      "\n" +
          "{\"legalName\":\"Gaby\",\"diseaseOrAgentTargeted\":\"Unknown\",\"startDateOfValidity\":\"2019-12-31\",\"personId\":\"ABC\",\"dateOfBirth\":\"2001-01-01\",\"gender\":\"F\",\"marketingAuthorizationHolder\":\"\",\"vaccineCode\":[{\"system\":\"urn:oid:1.2.36.1.2001.1005.17\"}],\"vaccineMedicinalProduct\":\"\",\"batchLotNumber\":\"\",\"dateOfVaccination\":\"\",\"administeringCentre\":\"\",\"healthProfessionalId\":\"\",\"countryOfVaccination\":\"\",\"numberInSeries\":\"\",\"nextVaccinationDate\":\"\",\"Total Matches\":70}";

  @BeforeAll
  static void beforeAll() throws Exception {

    cborPrivateKey = OneKey.generateKey(AlgorithmID.ECDSA_256);
    cborPublicKey = cborPrivateKey.PublicKey();

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
