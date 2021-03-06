package dev.xframe.http.request;

import dev.xframe.utils.XStrings;

public class HttpURI extends QueryString {

    public HttpURI(String uri) {
        super(uri, true);
    }
    
    public String xpath() {
        return XStrings.trim(path(), '/');
    }

}
