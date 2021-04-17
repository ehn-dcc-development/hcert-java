package ehn.techiop.hcert;

import ehn.techiop.hcert.model.CertificatePayload;
import ehn.techiop.hcert.model.HealthCertificate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertificateDSL {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DigitalGreenCertificate digitalGreenCertificate;

    public CertificateDSL() {
        digitalGreenCertificate = new DigitalGreenCertificate();
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
        digitalGreenCertificate.withVac(vacs);
        return this;
    }

    private static Date convert(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }

    public CertificateDSL withTestResult() {

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
                .withCou("DK");
        digitalGreenCertificate.getTst().add(test);
        return this;
    }

    public CertificateDSL withRecovery() {

        Rec recovery = new Rec()
                .withDis("840539006")
                .withDat(dateFormatter.format(LocalDate.now().minusDays(21)))
                .withCou("DK");

        digitalGreenCertificate.getRec().add(recovery);
        return this;
    }

    public CertificateDSL withSubject(String givenName, String familyName) {

        digitalGreenCertificate.withSub(new Sub()
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

        HealthCertificate healthCertificate = new HealthCertificate();
        healthCertificate.setDigitalGreenCertificate(digitalGreenCertificate);
        certificatePayload.setHcert(healthCertificate);
        return certificatePayload;
    }

    public CertificateDSL withExpiredRecovery() {

        Rec recovery = new Rec()
                .withDis("840539006")
                .withDat(dateFormatter.format(LocalDate.now().minusYears(1)))
                .withCou("DK");
        digitalGreenCertificate.getRec().add(recovery);
        return this;
    }

    public CertificateDSL withExpiredTestResult() {
        Tst test = new Tst()
                .withDis("840539006")
                .withTyp("LP6464-4")
                .withTna("Nucleic acid amplification with probe detection")
                .withTma("BIOSYNEX SWISS SA BIOSYNEX COVID-19 Ag BSS")
                .withOri("258500001")
                .withDtr(Math.toIntExact(LocalDateTime.now().minusDays(5).toEpochSecond(ZoneOffset.UTC)))
                .withDts(Math.toIntExact(LocalDateTime.now().minusDays(4).toEpochSecond(ZoneOffset.UTC)))
                .withRes("1240591000000102")
                .withFac("Region Midtjylland")
                .withCou("DK");


        digitalGreenCertificate.getTst().add(test);

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
        digitalGreenCertificate.withVac(vacs);
        return this;
    }

    public CertificatePayload expiredBuild() {

        CertificatePayload certificatePayload = getCertificatePayload(LocalDateTime.now().minusDays(6), LocalDateTime.now().minusDays(3));
        return certificatePayload;
    }
}
