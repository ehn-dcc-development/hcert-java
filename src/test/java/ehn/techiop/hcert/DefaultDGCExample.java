package ehn.techiop.hcert;

import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.payload.v1.Id;
import se.digg.dgc.payload.v1.Sub;
import se.digg.dgc.payload.v1.Vac;

import java.time.LocalDate;
import java.util.Arrays;

public class DefaultDGCExample {

    public static se.digg.dgc.payload.v1.DigitalGreenCertificate getTestDGC() {
        se.digg.dgc.payload.v1.DigitalGreenCertificate dgc = new DigitalGreenCertificate();

        se.digg.dgc.payload.v1.Sub sub = new Sub();
        sub
                .withGn("Martin")
                .withFn("Lindström")
                .withDob(LocalDate.parse("1969-11-29"))
                .withGen("male");

        se.digg.dgc.payload.v1.Id pnr = new se.digg.dgc.payload.v1.Id();
        pnr.withI("NN").withT("196911292932");
        se.digg.dgc.payload.v1.Id passport = new Id();
        passport.withI("PPN").withT("56987413");
        sub.withId(Arrays.asList(pnr, passport));

        dgc.setSub(sub);

        se.digg.dgc.payload.v1.Vac vac = new Vac();
        vac
                .withDis("Covid-19")
                .withVap("vap-value")
                .withMep("mep-value")
                .withAut("aut-value")
                .withSeq(Integer.valueOf(1))
                .withTot(Integer.valueOf(2))
                .withDat(LocalDate.parse("2021-04-02"))
                .withCou("SE");

        dgc.setVac(Arrays.asList(vac));

        return dgc;
    }
}
