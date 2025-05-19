package com.rits.customdataformatservice.service;

import com.rits.customdataformatservice.dto.CustomDataFormatRequest;
import com.rits.customdataformatservice.dto.CustomDataFormatResponseList;
import com.rits.customdataformatservice.exception.CustomDataFormatException;
import com.rits.customdataformatservice.model.*;
import com.rits.customdataformatservice.repository.CustomDataFormatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@Service
@RequiredArgsConstructor
public class CustomDataFormatServiceImpl implements CustomDataFormatService {
    private static final long serialVersionUID = 1L;

    private final CustomDataFormatRepository customDataFormatRepository;
//    private final WebClient.Builder webClientBuilder;
    private final List<MultiScanObj> multiScanList = new ArrayList<MultiScanObj>();
    Map<String, String> dataValueMap = new HashMap<String, String>();
    private List<CustomFormatPojo> zcustomFrmtList = new ArrayList<CustomFormatPojo>();
    private String site;
    private String Code;
    private String value;
    private String Datafield;
    private String user;
    private List<MainCustomDataObject> finalCustomList = new ArrayList<MainCustomDataObject>();
    private List<CustomDataObject> customdataList = new ArrayList<CustomDataObject>();

    private String sfc;

    private String strippinginput;
    private String SerialFromat;
    private String MaterialFormat;
    private String replaceInput;
    private String format;


    @Override
    public List<MainCustomDataObject> isDataFormatPresent(DataFormatRequest request) throws ParseException {
        String sfc = "";
        Boolean present = false;

//        JSONParser parser = new JSONParser();
//
//
//        JSONObject json = (JSONObject) parser.parse(String.valueOf(request));
//        System.out.println(json);


        this.site = request.getSite();
        this.user = request.getUser();
        String dataString = request.getDatastring();

        finalCustomList = new ArrayList<MainCustomDataObject>();

        dataString = processString(dataString);
        if (dataString.length() > 2) {
            format = dataString.substring(0, 2);
        }
        if (zcustomFrmtList.isEmpty()) {
            present = true;
        }


        getComponent(present, dataString, format, SerialFromat, MaterialFormat);
        MainCustomDataObject dataobj = new MainCustomDataObject();
        dataobj.setCustomdataList(customdataList);
        finalCustomList.add(dataobj);

        return finalCustomList;
    }

    public void getComponent(Boolean present, String dataString, String format, String SerialFromat, String MaterialFormat) {

        Boolean skip = false;
        String serial = "";
        String materialnumber = "";
        Boolean customTablepresent = false;
        Boolean isSfc = true;
        customdataList = new ArrayList<CustomDataObject>();
        // For standard format coversion
        if (present) {
            if (dataString.contains("{EOT}") && (!present)) {
                // format = "00";
            }
            if (present && dataString.contains("{EOT}") || dataString.contains("[)>")) {
                if (dataString.contains("[)>-")) {
                    String[] formatearray = dataString.split("-");
                    format = formatearray[1].substring(0, 2);
                } else {
                    String[] formatearray = dataString.split(">");
                    format = formatearray[1].substring(0, 2);
                }
                isSfc = false;
            }
            if (present && dataString.contains("@")) {
                dataString = "@" + dataString;
                String dummy = dataString.replace("@", "{GS}");
                dataString = "[)>-" + dummy + "{RS}{EOT}";
                isSfc = false;
            }

        }

        if (!present) {
            Map<String, String> list = customFormatParser(dataString);
            for (Map.Entry<String, String> entry : list.entrySet()) {
                CustomDataObject customobj = new CustomDataObject();
                customobj.setCode(format);
                customobj.setDataField(entry.getKey());
                customobj.setValue(entry.getValue());
                customdataList.add(customobj);

            }

            boolean empty = customdataList.isEmpty();
            if (!empty) {
                customTablepresent = true;
                skip = true;
            }
        }


    }

    @Override
    public Map<String, String> customFormatParser(String data) {
        // TODO Auto-generated method stub
        String scannedBarcode = data;
        String code = data.substring(0, 2);
        dataValueMap = new HashMap<String, String>();
        List<CustomFormatPojo> customFormat = findCustomFormatpojo(code);
        int forceExit = 0;
        while (scannedBarcode.length() > 0) {
            forceExit++;
            scannedBarcode = findCodeValue(scannedBarcode, code);
            code = findCode(scannedBarcode);
            if (forceExit > zcustomFrmtList.size()) {
                if (forceExit == zcustomFrmtList.size() - 1) {

                    break;
                }
                break;
            }


        }

        return dataValueMap;
    }

    private String findCode(String data) {
        String code = "";
        Iterator<CustomFormatPojo> customFormatIterator = zcustomFrmtList.iterator();
        while (customFormatIterator.hasNext()) {
            CustomFormatPojo zcustomFormatPojo = (CustomFormatPojo) customFormatIterator.next();
            if (!zcustomFormatPojo.getValidated()) {
                Pattern p = Pattern.compile("^" + zcustomFormatPojo.getCharacter());
                Matcher m = p.matcher(data);
                if (m.find()) {
                    code = zcustomFormatPojo.getCharacter();
                    break;
                }
            }

        }
        if (code.equalsIgnoreCase("")) {

        }

        return code;
    }

    private String findCodeValue(String data, String code) {
        String value = null;
        data = data.replace("GS", "@");
        String returnString = data;
        Iterator<CustomFormatPojo> customFormatIterator = zcustomFrmtList.iterator();
        while (customFormatIterator.hasNext()) {
            CustomFormatPojo customFormatPojo = (CustomFormatPojo) customFormatIterator.next();
            if (code.equalsIgnoreCase(customFormatPojo.getCharacter())) {
                Pattern p = Pattern.compile("^" + customFormatPojo.getCharacter());
                Matcher m = p.matcher(data);
                if (m.find()) {
                    if ((customFormatPojo.getLeading_char() != null) && (!customFormatPojo.getLeading_char().equalsIgnoreCase(""))) {
                        value = data.substring(customFormatPojo.getLeading_char().length());

                        String formatearray[] = data.split(customFormatPojo.getLeading_char());
                        value = formatearray[0];

                        value = value.substring(customFormatPojo.getCharacter().length());
                        if (value.startsWith("@")) {
                            String valuearray[] = value.split(customFormatPojo.getCharacter());
                            value = valuearray[1];
                        }
                        /*
                         * String valuearray[] =
                         * value.split(customFormatPojo.getCharacter()); value
                         * = valuearray[1];
                         */

                        Pattern p1 = Pattern.compile(customFormatPojo.getLeading_char());
                        Matcher m1 = p1.matcher(returnString);
                        if (m1.find()) {
                            returnString = returnString.substring(m1.start() + customFormatPojo.getLeading_char().length());
                            if (returnString.startsWith("@")) {
                                returnString = returnString.substring(1);
                            }
                        }
                    } else if (!(customFormatPojo.getFixed_len() == 0)) {
                        value = data.substring(0, customFormatPojo.getFixed_len() + code.length());
                        value = value.substring(customFormatPojo.getCharacter().length());

                        /*
                         * String valuearray[] =
                         * value.split(customFormatPojo.getCharacter());
                         *
                         * value = valuearray[1];
                         */
                        returnString = returnString.substring(customFormatPojo.getFixed_len() + code.length());

                    } else if ((customFormatPojo.getLeading_char() == null) || (customFormatPojo.getLeading_char().equalsIgnoreCase("")) || (customFormatPojo.getFixed_len() == 0)) {
                        value = data;
                        value = value.substring(customFormatPojo.getCharacter().length());

                        /*
                         * String valuearray[] =
                         * value.split(customFormatPojo.getCharacter()); value
                         * = valuearray[1];
                         */

                        returnString = "";
                    }
                    dataValueMap.put(customFormatPojo.getData_field(), value);
                    customFormatPojo.setValidated(true);
                    break;
                }
                // Matcher m = p.matcher("0100630414167275 11220404
                // 21804110A5204@24011223173@422276@2006");
            }


        }
        System.out.println("RETURNED STR: " + returnString);
        return returnString;

    }


    // if (!dataString.contains("@") && !dataString.contains("{EOT}")) {


    private List<CustomFormatPojo> findCustomFormatpojo(String code) {
        zcustomFrmtList = new ArrayList<CustomFormatPojo>();

        try {
            List<CustomDataFormat> itemSet = customDataFormatRepository.findByCodeAndSiteAndActiveOrderBySequence(code, site, 1);
            if (itemSet != null) {
                for (CustomDataFormat customDataFormat : itemSet) {
                    CustomFormatPojo customFormatPojo = new CustomFormatPojo();
                    customFormatPojo.setHandle(customDataFormat.getHandle());
                    customFormatPojo.setCode(customDataFormat.getCode());
                    customFormatPojo.setDescription(customDataFormat.getDescription());
                    customFormatPojo.setCharacter(customDataFormat.getCharacter());
                    customFormatPojo.setData_field(customDataFormat.getDataField());
                    customFormatPojo.setFixed_len(customDataFormat.getFixedLength());
                    customFormatPojo.setLeading_char(customDataFormat.getLeadingCharacter());
                    customFormatPojo.setValidated(false);
                    zcustomFrmtList.add(customFormatPojo);
                }
            }
        } catch (Exception e) {
            throw e;
        }

        return zcustomFrmtList;
    }


    private String processString(String inputString) {
        if (!inputString.contains("@")) {

            try {
                inputString = URLEncoder.encode(inputString, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        CustomDataFormat customDataFormat = customDataFormatRepository.findByActiveAndSiteAndDataField(1, site, "STRIPPING_END");
        strippinginput = customDataFormat.getLeadingCharacter();
        String[] trimValues = strippinginput.split(",");
        for (String trimValue : trimValues) {
            inputString = inputString.replace(trimValue, "");
        }
        if (inputString.length() > 2) {
            format = inputString.substring(0, 2);
        }
        zcustomFrmtList = findCustomFormatpojo(format);

        if (format.equalsIgnoreCase("01")) {
            CustomDataFormat customDataFormatObj = customDataFormatRepository.findByActiveAndCodeAndSiteAndDataField(1, format, site, "REPLACEABLE");
            replaceInput = customDataFormatObj.getLeadingCharacter();
        }

        CustomDataFormat customDataFormatObj1 = customDataFormatRepository.findByActiveAndCodeAndSiteAndDataField(1, format, site, "SERIAL");

        if (customDataFormatObj1 == null) {
            throw new CustomDataFormatException(5003);
        }


        SerialFromat = customDataFormatObj1.getLeadingCharacter();

//        CustomDataFormat customDataFormatObj2 = customDataFormatRepository.findByActiveAndCodeAndSiteAndDataField(1, format, site, "MATERIALNUM");
//        MaterialFormat = customDataFormatObj2.getLeadingCharacter();

        if (replaceInput != null || replaceInput == "") {
            String[] replaceValues = replaceInput.split(",");
            for (String replaceValue : replaceValues) {
                inputString = inputString.replace(replaceValue, "@");
            }
            try {
                inputString = URLDecoder.decode(inputString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return inputString;
    }


    @Override
    public List<MultiScanObj> getLeadingCharacterList(String Format, String site) {
        return null;
    }

    @Override
    public MessageModel createCustomDataFormat(CustomDataFormatRequest customDataFormatRequest) throws Exception {
        return null;
    }

    @Override
    public CustomDataFormat retrieveCustomDataFormat(String site, String code, int sequence) throws Exception {
        return null;
    }

    @Override
    public CustomDataFormatResponseList getAllCustomDataFormat(String site) {
        return null;
    }
}
