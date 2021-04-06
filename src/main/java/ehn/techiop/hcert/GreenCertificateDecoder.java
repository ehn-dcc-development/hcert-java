package ehn.techiop.hcert;

import static org.apache.commons.compress.utils.IOUtils.toByteArray;

import COSE.CoseException;
import COSE.KeyKeys;
import COSE.Message;
import COSE.MessageTag;
import COSE.OneKey;
import COSE.Sign1Message;
import com.upokecenter.cbor.CBORObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;

import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class GreenCertificateDecoder {

  private final OneKey publicKey;

  public GreenCertificateDecoder(OneKey publicKey) throws CoseException {
    this((ECPublicKey) publicKey.AsPublicKey());
  }

  public GreenCertificateDecoder(ECPublicKey publicKey) throws CoseException {
    ECPoint ecpoint = publicKey.getW();

    int size = publicKey.getParams().getCurve().getField().getFieldSize();
    if(size != 256)
      throw new RuntimeException("Key size not supported");

    CBORObject key = CBORObject.NewMap();
    key.Add(KeyKeys.KeyType.AsCBOR(), KeyKeys.KeyType_EC2);
    key.Add(KeyKeys.EC2_Curve.AsCBOR(), KeyKeys.EC2_P256);
    key.Add(KeyKeys.EC2_X.AsCBOR(), ArrayFromBigNum(ecpoint.getAffineX(), size));
    key.Add(KeyKeys.EC2_Y.AsCBOR(), ArrayFromBigNum(ecpoint.getAffineY(), size));

    this.publicKey = new OneKey(key);

  }

  /**
   * Copied from OneKey
   *
   * @param n
   * @param curveSize
   * @return
   */
  static byte[] ArrayFromBigNum(BigInteger n, int curveSize) {
    byte[] rgb = new byte[(curveSize + 7) / 8];
    byte[] rgb2 = n.toByteArray();
    if (rgb.length == rgb2.length) {
      return rgb2;
    }
    if (rgb2.length > rgb.length) {
      System.arraycopy(rgb2, rgb2.length - rgb.length, rgb, 0, rgb.length);
    } else {
      System.arraycopy(rgb2, 0, rgb, rgb.length - rgb2.length, rgb2.length);
    }
    return rgb;
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
    byte[] decodedBytes = Base45.getDecoder().decode(base45String);

    byte[] coseBytes = getCoseBytes(decodedBytes);

    byte[] cborBytes = getCborBytes(coseBytes);

    return getJsonString(cborBytes);
  }

  private String getJsonString(byte[] cborBytes) throws IOException {
    CBORObject cborObject = CBORObject.DecodeFromBytes(cborBytes);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    cborObject.WriteJSONTo(byteArrayOutputStream);

    return byteArrayOutputStream.toString("UTF-8");
  }

  private byte[] getCborBytes(byte[] coseBytes) throws CoseException {
    Sign1Message msg = (Sign1Message) Message.DecodeFromBytes(coseBytes, MessageTag.Sign1);
    if (!msg.validate(publicKey)) {
      throw new RuntimeException("Could not verify COSE signature");
    }

    return msg.GetContent();
  }

  private byte[] getCoseBytes(byte[] decodedBytes) throws CompressorException, IOException {

    CompressorInputStream compressedStream = new CompressorStreamFactory()
        .createCompressorInputStream(CompressorStreamFactory.DEFLATE,
            new ByteArrayInputStream(decodedBytes));

    return toByteArray(compressedStream);
  }
}
