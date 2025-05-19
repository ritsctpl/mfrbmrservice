package com.rits.customdataformatservice.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rits.customdataformatservice.dto.CustomDataFormatRequest;
import com.rits.customdataformatservice.dto.CustomDataFormatResponseList;
import com.rits.customdataformatservice.model.*;
import org.json.simple.parser.ParseException;

public interface CustomDataFormatService {


    public List<MainCustomDataObject> isDataFormatPresent(DataFormatRequest request) throws ParseException;


    public Map<String, String> customFormatParser(String data);


    public List<MultiScanObj> getLeadingCharacterList(String Format, String site);

    public MessageModel createCustomDataFormat(CustomDataFormatRequest customDataFormatRequest) throws Exception;

    public CustomDataFormat retrieveCustomDataFormat(String site, String code, int sequence) throws Exception;

    public CustomDataFormatResponseList getAllCustomDataFormat(String site);
}
