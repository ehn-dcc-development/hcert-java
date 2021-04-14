package ehn.techiop.hcert;

import ehn.techiop.hcert.model.CertificatePayload;
import ehn.techiop.hcert.model.Hcert;
import ehn.techiop.hcert.schema.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertificateDSL {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final EuHcertV1Schema euHcertV1Schema;

    public CertificateDSL() {
        euHcertV1Schema = new EuHcertV1Schema();
    }

    public CertificateDSL withVaccine() {

        List<Vac> vacs = new ArrayList<>();
        vacs.add(new Vac()
                .withDis("840539006")
                .withVap("1119305005")
                .withMep("EU/1/20/1528")
                .withAut("ORG-100030215")
                .withSeq(1)
                .withTot(2)
                .withDat(dateFormatter.format(LocalDate.now().minusDays(35)))
                .withCou("DK"));
        vacs.add(new Vac()
                .withDis("840539006")
                .withVap("1119305005")
                .withMep("EU/1/20/1528")
                .withAut("ORG-100030215")
                .withSeq(2)
                .withTot(2)
                .withDat(dateFormatter.format(LocalDate.now().minusDays(7)))
                .withCou("DK"));
        euHcertV1Schema.withVac(vacs);
        return this;
    }

    private static Date convert(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }

    public CertificateDSL withTestResult() {

        Date sampleDate = convert(LocalDateTime.now().minusDays(1));
        Date testResult = convert(LocalDateTime.now().minusHours(4));

        Tst test = new Tst()
                .withDis("840539006")
                .withTyp("LP6464-4")
                .withTna("Nucleic acid amplification with probe detection")
                .withTma("BIOSYNEX SWISS SA BIOSYNEX COVID-19 Ag BSS")
                .withOri("258500001")
                .withDtr(sampleDate)
                .withDts(testResult)
                .withRes("1240591000000102")
                .withFac("Region Midtjylland")
                .withCou("DK");
        euHcertV1Schema.withTst(test);
        return this;
    }

    public CertificateDSL withRecovery() {

        Rec recovery = new Rec()
                .withDis("840539006")
                .withDat(dateFormatter.format(LocalDate.now().minusDays(21)))
                .withCou("DK");

        euHcertV1Schema.withRec(recovery);
        return this;
    }

    public CertificateDSL withSubject(String givenName, String familyName) {

        euHcertV1Schema.withSub(new Sub()
                .withGn(givenName)
                .withFn(familyName)
                .withGen("female")
                .withDob(dateFormatter.format(LocalDate.now().minusYears(35))));
        return this;
    }

    public CertificatePayload build() {
        CertificatePayload certificatePayload = getCertificatePayload(LocalDateTime.now(), LocalDateTime.now().plusDays(3));
        return certificatePayload;
    }

    private CertificatePayload getCertificatePayload(LocalDateTime issuedAt, LocalDateTime expires) {
        CertificatePayload certificatePayload = new CertificatePayload();
        certificatePayload.setIss("DK");

        certificatePayload.setIat(issuedAt.toEpochSecond(ZoneOffset.UTC));
        certificatePayload.setExp(expires.toEpochSecond(ZoneOffset.UTC));

        Hcert hcert = new Hcert();
        hcert.setEuHcertV1Schema(euHcertV1Schema);
        certificatePayload.setHcert(hcert);
        return certificatePayload;
    }

    public CertificateDSL withExpiredRecovery() {
        withRecovery();
        euHcertV1Schema.getRec().setDat(dateFormatter.format(LocalDate.now().minusYears(1)));
        return this;
    }

    public CertificateDSL withExpiredTestResult() {
        withTestResult();

        Date sampleDate = convert(LocalDateTime.now().minusDays(5));
        Date testResult = convert(LocalDateTime.now().minusDays(4));

        euHcertV1Schema.getTst().setDtr(sampleDate);
        euHcertV1Schema.getTst().setDts(testResult);

        return this;
    }

    public CertificateDSL withExpiredVaccine() {

        List<Vac> vacs = new ArrayList<>();
        vacs.add(new Vac()
                .withDis("840539006")
                .withVap("1119305005")
                .withMep("EU/1/20/1528")
                .withAut("ORG-100030215")
                .withSeq(1)
                .withTot(2)
                .withDat(dateFormatter.format(LocalDate.now().minusDays(300)))
                .withCou("DK"));
        vacs.add(new Vac()
                .withDis("840539006")
                .withVap("1119305005")
                .withMep("EU/1/20/1528")
                .withAut("ORG-100030215")
                .withSeq(2)
                .withTot(2)
                .withDat(dateFormatter.format(LocalDate.now().minusDays(272)))
                .withCou("DK"));
        euHcertV1Schema.withVac(vacs);
        return this;
    }

    public CertificatePayload expiredBuild() {

        CertificatePayload certificatePayload = getCertificatePayload(LocalDateTime.now().minusDays(6), LocalDateTime.now().minusDays(3));
        return certificatePayload;
    }
}
