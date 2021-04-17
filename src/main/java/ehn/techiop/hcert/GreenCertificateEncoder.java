package ehn.techiop.hcert;

import COSE.Attribute;
import COSE.CoseException;
import COSE.HeaderKeys;
import COSE.KeyKeys;
import COSE.OneKey;
import COSE.Sign1Message;
import com.upokecenter.cbor.CBORObject;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import nl.minvws.encoding.Base45;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GreenCertificateEncoder {

    private final OneKey privateKey;
    private final String kid;
    private final String prefix;

    public GreenCertificateEncoder(OneKey privateKey, String kid) {
        this(privateKey, kid, "HC1");
    }

    public GreenCertificateEncoder(OneKey privateKey, String kid, String prefix) {
        this.privateKey = privateKey;
        this.kid = kid;
        this.prefix = prefix;
    }

    /**
     * Encodes arbitrary Json String to CBOR -> COSE -> Deflate -> BASE45
     *
     * @param json
     * @return
     * @throws CoseException
     * @throws CompressorException
     * @throws IOException
     */
    public String encode(String json) throws CoseException, CompressorException, IOException {

        byte[] cborBytes = toCBORbytes(json);

        byte[] coseBytes = getCOSEBytes(cborBytes);

        byte[] deflateBytes = compress(coseBytes);

        return getBase45(deflateBytes);
    }

    private String getBase45(byte[] deflateBytes) {

        return prefix + Base45.getEncoder().encodeToString(deflateBytes);
    }

    private byte[] compress(byte[] messageBytes) throws CompressorException, IOException {
        ByteArrayOutputStream deflateOutputStream = new ByteArrayOutputStream();
        try (CompressorOutputStream deflateOut = new CompressorStreamFactory()
                .createCompressorOutputStream(CompressorStreamFactory.DEFLATE, deflateOutputStream)) {

            deflateOut.write(messageBytes);
        }

        return deflateOutputStream.toByteArray();
    }

    private byte[] getCOSEBytes(byte[] cborBytes) throws CoseException {
        Sign1Message msg = new Sign1Message();
        msg.addAttribute(HeaderKeys.Algorithm, privateKey.get(KeyKeys.Algorithm), Attribute.PROTECTED);
        msg.addAttribute(HeaderKeys.KID, CBORObject.FromObject(kid), Attribute.PROTECTED);
        msg.SetContent(cborBytes);
        msg.sign(privateKey);

        return msg.EncodeToBytes();
    }

    private byte[] toCBORbytes(String json) {
        CBORObject cborObject = CBORObject.FromJSONString(json);
        return cborObject.EncodeToBytes();
    }
}
