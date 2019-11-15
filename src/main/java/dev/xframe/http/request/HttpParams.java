package dev.xframe.http.request;

import java.util.List;
import java.util.Set;

import dev.xframe.utils.XStrings;

public interface HttpParams {
    
    Set<String>  getParamNames();
    List<String> getParamValues(String name);
    
    default String getParam(String name) {
        List<String> values = getParamValues(name);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }
    default String getParam(String name, String def) {
        String val = getParam(name);
        return XStrings.isEmpty(val) ? def : val;
    }

    default int getParamAsInt(String name) {
        return getParamAsInt(name, -1);
    }
    default int getParamAsInt(String name, int def) {
        String param = getParam(name);
        return XStrings.isEmpty(param) ? def : Integer.parseInt(param);
    }
    
    default long getParamAsLong(String name) {
        return getParamAsLong(name, -1);
    }
    default long getParamAsLong(String name, long def) {
        String param = getParam(name);
        return XStrings.isEmpty(param) ? def : Long.parseLong(param);
    }

}