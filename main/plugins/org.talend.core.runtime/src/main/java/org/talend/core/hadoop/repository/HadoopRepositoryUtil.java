// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.hadoop.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.utils.json.JSONArray;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * created by ycbai on 2013-10-24 Detailled comment
 * 
 */
public class HadoopRepositoryUtil {

    /**
     * DOC ycbai Comment method "getHadoopPropertiesJsonStr".
     * 
     * <p>
     * Get the json string type of hadoop properties from list type.
     * </p>
     * 
     * @param properties
     * @return
     * @throws JSONException
     */
    public static String getHadoopPropertiesJsonStr(List<Map<String, Object>> properties) {
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray();
            if (properties != null && properties.size() > 0) {
                for (Map<String, Object> map : properties) {
                    JSONObject object = new JSONObject();
                    Iterator<String> it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        object.put(key, map.get(key));
                    }
                    jsonArr.put(object);
                }
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }

        return jsonArr.toString();
    }

    public static List<Map<String, Object>> getHadoopPropertiesList(String propertiesJsonStr) {
        return getHadoopPropertiesList(propertiesJsonStr, false);
    }

    /**
     * DOC ycbai Comment method "getHadoopPropertiesList".
     * 
     * <p>
     * Get the list type of hadoop properties from json string.
     * </p>
     * 
     * @param propertiesJsonStr
     * @param includeQuotes
     * @return
     * @throws JSONException
     */
    public static List<Map<String, Object>> getHadoopPropertiesList(String propertiesJsonStr, boolean includeQuotes) {
        List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
        try {
            if (StringUtils.isNotEmpty(propertiesJsonStr)) {
                JSONArray jsonArr = new JSONArray(propertiesJsonStr);
                for (int i = 0; i < jsonArr.length(); i++) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    JSONObject object = jsonArr.getJSONObject(i);
                    Iterator<String> it = object.keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        String value = String.valueOf(object.get(key));
                        if (includeQuotes) {
                            value = TalendQuoteUtils.addQuotesIfNotExist(value);
                        } else {
                            value = TalendQuoteUtils.removeQuotesIfExist(value);
                        }
                        map.put(key, value);
                    }
                    properties.add(map);
                }
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }

        return properties;
    }

    /**
     * DOC ycbai Comment method "getHadoopPropertiesFullList".
     * 
     * <p>
     * Get the full list type of hadoop properties (with the parent properties of the hadoop cluster it belongs) from
     * json string.
     * </p>
     * 
     * @param connection
     * @param propertiesJsonStr
     * @param includeQuotes
     * @return
     */
    public static List<Map<String, Object>> getHadoopPropertiesFullList(Connection connection, String propertiesJsonStr,
            boolean includeQuotes) {
        IHadoopClusterService hadoopClusterService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IHadoopClusterService.class)) {
            hadoopClusterService = (IHadoopClusterService) GlobalServiceRegister.getDefault().getService(
                    IHadoopClusterService.class);
        }
        List<Map<String, Object>> parentProperties = null;
        if (hadoopClusterService != null) {
            parentProperties = getHadoopPropertiesList(hadoopClusterService.getHadoopClusterProperties(connection), true);
        }
        List<Map<String, Object>> properties = getHadoopPropertiesList(propertiesJsonStr, true);
        Map<String, Map<String, Object>> propertiesMap = new HashMap<String, Map<String, Object>>();
        for (Map<String, Object> proMap : properties) {
            String property = String.valueOf(proMap.get("PROPERTY")); //$NON-NLS-1$
            propertiesMap.put(property, proMap);
        }
        if (parentProperties != null) {
            for (Map<String, Object> parentProMap : parentProperties) {
                String property = String.valueOf(parentProMap.get("PROPERTY")); //$NON-NLS-1$
                if (!propertiesMap.containsKey(property)) {
                    propertiesMap.put(property, parentProMap);
                    properties.add(parentProMap);
                }
            }
        }

        return properties;
    }

}
