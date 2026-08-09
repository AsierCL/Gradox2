package com.example.gradox2.service.interfaces;

public interface FileUrlSigner {

    String presignedGetUrl(String key, String contentDisposition);
}