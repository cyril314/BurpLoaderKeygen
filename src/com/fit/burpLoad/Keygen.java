package com.fit.burpLoad;


import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

public class Keygen {

    private static String getRandomString() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder str = new StringBuilder();
        Random rnd = new Random();
        while (str.length() < 32) {
            int index = (int) (rnd.nextFloat() * CHARS.length());
            str.append(CHARS.charAt(index));
        }
        return str.toString();
    }

    private static ArrayList<String> getParamsList(String data) {
        byte[] rawBytes = SignUtils.decrypt(Base64.getDecoder().decode(data));
        ArrayList<String> ar = new ArrayList<>();
        int from = 0;
        for (int i = 0; i < rawBytes.length; i++) {
            if (rawBytes[i] == 0) {
                ar.add(new String(rawBytes, from, i - from));
                from = i + 1;
            }
        }
        ar.add(new String(rawBytes, from, rawBytes.length - from));
        return ar;
    }

    private static ArrayList<String> decodeActivationRequest(String activationRequest) {
        try {
            ArrayList<String> ar = getParamsList(activationRequest);
            if (ar.size() != 5) {
                System.out.print("Activation Request Decoded to wrong size! The following was Decoded: \n" + ar);
            } else {
                return ar;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String privateKey2048 = "30820238020100300D06092A864886F70D0101010500048202223082021E02010002820101009D9DF9EB49890DE8F193C89598584BC947BA83727B2D89AA8BE3A4689130FE2E948967D40B656762F9989E59C9655E28E33FD4B4A544126FDD90A566BB61C2D7C74A6829265767B56E28FD2214D4BEB3B1DA4722BC394E2E6AFA0F1689FA9DB442643DDDA84997C5AD15B57EE5BD1A357CABF6ED4CAAA5FB8872E07C8F5FAE1C573C1214DD273C6D8887D7E993208D75118CC2305D60AA337B0999B69988322A8FAA9FBFF49AB70B71723E1CBD79D12640AF19E6FBC28C05E6630414DBAD9AEF912D0AC53E40B7F48EE29BFE1DEFCFB0BDB1B6C5BF8B06DCCA15FA1FC3F468952D481070C92C386D3CE6187B062038A6CA822D352ECEBEAC195918F9BB5C3AC3020100028201005DAD71C754BA3F692E835E1903259F4D6EF33C82C3110A9C316E47DDDA455B1D062D306787AA6A2B1A1B8A29E517F941A5E6DF1DCA87CDC96CCF366EFB799C1B31185915F3F2C8F1BD1A61706B1F1284AC7506087004432235748F991EC2B40E59D3482DC08294D0E9115900A5BCA1A21E89FA45896677262B2FD39A54805273162D655F1AB4392CE4E01A4DD63F7EF387B79D53B73BBE45EA7D9BE64A627CFB3DAE2843E85ED3697672BD4832F5EEB4C18C4D15FEB550E0B5A7018A3CD39A9FD4BDA35A6F88BD00CCBC787419AD57C54FA823EC3D7662710B03C2622E9E2DE546B21CA1C76672B1CC6BD92871A0F96051E31CB060E0DDB4022BEB2897A88761020100020100020100020100020100";
    private static final String privateKey1024 = "30820136020100300D06092A864886F70D0101010500048201203082011C020100028181008D187233EB87AB60DB5BAE8453A7DE035428EB177EC8C60341CAB4CF487052751CA8AFF226EA3E98F0CEEF8AAE12E3716B8A20A24BDE20703865C9DBD9543F92EA6495763DFD6F7507B8607F2A14F52694BB9793FE12D3D9C5D1C0045262EA5E7FA782ED42568C6B7E31019FFFABAEFB79D327A4A7ACBD4D547ACB2DC9CD04030201000281807172A188DBAD977FE680BE3EC9E0E4E33A4D385208F0383EB02CE3DAF33CD520332DF362BA2588B58292710AC9D2882C4F329DF0C11DD66944FF9B21F98A031ED27C19FE2BCF8A09AD3E254A0FD7AB89E0D1E756BCF37ED24D42D1977EA7C1C78ABF4D13F752AE48B426A2DC98C5D13B2313609FAA6441E835DC61D17A01D1A9020100020100020100020100020100";

    private static final String SHA256withRSA = "xMoYxfewJJ3jw/Zrqghq1nMHJIsZEtZLu9kp4PZw+kGt+wiTtoUjUfHyTt/luR3BjzVUj2Rt2tTxV2rjWcuV7MlwsbFrLOqTVGqstIYA1psSP/uspFkkhFwhMi0CJNRHdxe+xPYnXObzi/x6G4e0wH3iZ5bnYPRfn7IHiV1TVzslQur/KR5J8BG8CN3B9XaS8+HJ90Hn4sy81fW0NYRlNW8m5k4rMDNwCLvDzp11EN//wxYEdruNKqtxEvv6VesiFOg711Y6g/9Nf91C5dFedNEhPv2k2fYb4rJ+z1mCOBSmWIzjGlS1r2xOzITrrrMkr+ilBE3VFPPbES4KsRh/fw==";
    private static final String SHA1withRSA = "tdq99QBI3DtnQQ7rRJLR0uAdOXT69SUfAB/8O2zi0lsk4/bXkM58TP6cuhOzeYyrVUJrM11IsJhWrv8SiomzJ/rqledlx+P1G5B3MxFVfjML9xQz0ocZi3N+7dHMjf9/jPuFO7KmGfwjWdU4ItXSHFneqGBccCDHEy4bhXKuQrA=";

    public static String generateLicense(String licenseName, boolean isOld) {
        LocalDate localDate = LocalDate.of(2099, 12, 30);
        Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        long unixTimestamp = instant.toEpochMilli();

        ArrayList<String> licenseArray = new ArrayList<>();
        licenseArray.add(getRandomString());
        licenseArray.add("license");
        licenseArray.add(licenseName);
        licenseArray.add(String.valueOf(unixTimestamp));
        licenseArray.add("1");
        licenseArray.add("full");
        if (isOld) {
            licenseArray.add(SHA1withRSA);
        } else {
            licenseArray.add(SignUtils.getSign(privateKey1024, getSignatureBytes(licenseArray), "SHA1withRSA"));
        }
        System.out.println(ConvertJson.toJson(licenseArray));
        return prepareArray(licenseArray);
    }

    private static String prepareArray(ArrayList<String> list) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            for (int i = 0; i < list.size() - 1; i++) {
                byteArray.write(list.get(i).getBytes());
                byteArray.write(0);
            }
            byteArray.write(list.get(list.size() - 1).getBytes());
            return new String(Base64.getEncoder().encode(SignUtils.encrypt(byteArray.toByteArray())));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String generateActivation(String activationRequest, boolean isOld) {
        ArrayList<String> request = decodeActivationRequest(activationRequest);
        if (request == null) {
            return "Error decoding activation request :-(";
        }
        ArrayList<String> responseArray = new ArrayList<>();
        responseArray.add("0.4315672535134567");
        responseArray.add(request.get(0));
        responseArray.add("activation");
        responseArray.add(request.get(1));
        responseArray.add("True");
        responseArray.add("");
        responseArray.add(request.get(2));
        responseArray.add(request.get(3));
        if (isOld) {
            responseArray.add(SHA256withRSA);
            responseArray.add(SHA1withRSA);
        } else {
            responseArray.add(SignUtils.getSign(privateKey2048, getSignatureBytes(responseArray), "SHA256withRSA"));
            responseArray.add(SignUtils.getSign(privateKey1024, getSignatureBytes(responseArray), "SHA1withRSA"));
        }
        System.out.println(ConvertJson.toJson(responseArray));
        return prepareArray(responseArray);
    }

    private static byte[] getSignatureBytes(ArrayList<String> list) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            for (String s : list) {
                byteArray.write(s.getBytes());
                byteArray.write(0);
            }
            return byteArray.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}