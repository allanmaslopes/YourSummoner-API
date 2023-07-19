package com.yoursummoner.api.model.error;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "Error")
public class ErrorXmlApi {


    @JacksonXmlElementWrapper(namespace = "code", localName = "Code")
    public String code;

    @JacksonXmlElementWrapper(namespace = "message", localName = "Message")
    public String message;

    @JacksonXmlElementWrapper(namespace = "requestId", localName = "RequestId")
    public String requestId;

    @JacksonXmlElementWrapper(namespace = "hostId", localName = "HostId")
    public String hostId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
