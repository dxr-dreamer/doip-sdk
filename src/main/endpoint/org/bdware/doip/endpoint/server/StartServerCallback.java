package org.bdware.doip.endpoint.server;

public interface StartServerCallback {
    public void onSuccess(int port);
    void onException(Exception e);
}
