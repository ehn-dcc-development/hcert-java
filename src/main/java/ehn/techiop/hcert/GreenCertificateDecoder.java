package ehn.techiop.hcert;

import static org.apache.commons.compress.utils.IOUtils.toByteArray;

import COSE.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upokecenter.cbor.CBORObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ehn.techiop.hcert.model.CertificatePayload;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class GreenCertificateDecoder {

    private final CertificateProvider certificateProvider;
    private final String prefix;

    public GreenCertificateDecoder(CertificateProvider certificateProvider) {
        this(certificateProvider, "HC1");
    }

    public GreenCertificateDecoder(CertificateProvider certificateProvider, String prefix) {
        this.certificateProvider = certificateProvider;
        this.prefix = prefix;
    }


    /**
     * Decodes base45 encoded string -> Deflate -> COSE -> CBOR -> arbitrary Json String
     *
     * @param base45String
     * @return
     * @throws CompressorException
     * @throws IOException
     * @throws CoseException
     */
    public String decode(String base45String) throws CompressorException, IOException, CoseException {
        if (!base45String.startsWith(prefix))
            throw new RuntimeException("Base45 string not valid according to specification");

        base45String = base45String.substring(3);
        byte[] decodedBytes = Base45.getDecoder().decode(base45String);

        byte[] coseBytes = decompress(decodedBytes);

        return toJsonPayload(coseBytes);
    }

    private String toJsonPayload(byte[] coseBytes) throws CoseException, IOException {
        Sign1Message msg = (Sign1Message) Message.DecodeFromBytes(coseBytes, MessageTag.Sign1);

        byte[] kid = msg.getProtectedAttributes().get(HeaderKeys.KID.AsCBOR()).GetByteString();

        CBORObject cborObject = CBORObject.DecodeFromBytes(msg.GetContent());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        cborObject.WriteJSONTo(byteArrayOutputStream);

        String json = byteArrayOutputStream.toString("UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        CertificatePayload certificatePayload = objectMapper.readValue(json, CertificatePayload.class);
        String issuer = certificatePayload.getIss();


        if (!msg.validate(new OneKey(certificateProvider.provideKey(kid, issuer), null))) {
            throw new RuntimeException("Could not verify COSE signature");
        }

        return json;
    }

    private byte[] decompress(byte[] decodedBytes) throws CompressorException, IOException {

        CompressorInputStream compressedStream = new CompressorStreamFactory()
                .createCompressorInputStream(CompressorStreamFactory.DEFLATE,
                        new ByteArrayInputStream(decodedBytes));

        return toByteArray(compressedStream);
    }
}
