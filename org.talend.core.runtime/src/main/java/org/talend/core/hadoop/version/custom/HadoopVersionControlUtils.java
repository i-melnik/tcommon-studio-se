// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.hadoop.version.custom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EMap;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;

/**
 * Created by Marvin Wang on Mar 26, 2013.
 */
public class HadoopVersionControlUtils {

    public final static String JAR_SEPARATOR = ";"; //$NON-NLS-1$

    public final static String EMPTY_STR = ""; //$NON-NLS-1$

    public static Map<String, Set<String>> getCustomVersionMap(DatabaseConnection connection) {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        if (connection == null) {
            return map;
        }

        EMap<String, String> parameters = connection.getParameters();
        if (parameters.size() == 0) {
            return map;
        }

        ECustomVersionGroup[] values = ECustomVersionGroup.values();
        for (ECustomVersionGroup group : values) {
            String groupName = group.getName();
            String jarString = parameters.get(groupName);
            if (jarString != null && !jarString.isEmpty()) {
                Set<String> jarSet = new HashSet<String>();
                String[] jarArray = jarString.split(JAR_SEPARATOR);
                for (String jar : jarArray) {
                    jarSet.add(jar);
                }
                map.put(groupName, jarSet);
            }
        }

        return map;
    }

    public static void injectCustomVersionMap(DatabaseConnection connection, Map<String, Set<String>> map) {
        if (connection == null || map == null) {
            return;
        }
        EMap<String, String> parameters = connection.getParameters();
        // remove previous custom param
        for (String group : map.keySet()) {
            if (parameters.keySet().contains(group)) {
                parameters.put(group, EMPTY_STR);
            }
        }
        Iterator<Entry<String, Set<String>>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Set<String>> entry = iter.next();
            String groupName = entry.getKey();
            Set<String> jars = entry.getValue();
            if (jars != null && jars.size() > 0) {
                StringBuffer jarBuffer = new StringBuffer();
                for (String jar : jars) {
                    jarBuffer.append(jar).append(JAR_SEPARATOR);
                }
                if (jarBuffer.length() > 0) {
                    jarBuffer.deleteCharAt(jarBuffer.length() - 1);
                    parameters.put(groupName, jarBuffer.toString());
                }
            }
        }
    }

    public static String getCompCustomJarsParamFromRep(DatabaseConnection connection, ECustomVersionGroup versionGroup) {
        if (connection == null || versionGroup == null) {
            return EMPTY_STR;
        }
        EMap<String, String> parameters = connection.getParameters();
        if (parameters.size() == 0) {
            return EMPTY_STR;
        }

        return parameters.get(versionGroup.getName());
    }

    public static Map<String, Set<String>> getRepCustomJarParamFromComp(String compCustomJars, ECustomVersionGroup versionGroup) {
        Map<String, Set<String>> customVersionMap = new HashMap<String, Set<String>>();
        if (StringUtils.isEmpty(compCustomJars)) {
            return customVersionMap;
        }
        Set<String> jarSet = new HashSet<String>();
        String[] jarArray = compCustomJars.split(JAR_SEPARATOR);
        for (String jar : jarArray) {
            jarSet.add(jar);
        }
        customVersionMap.put(versionGroup.getName(), jarSet);

        return customVersionMap;
    }
}
