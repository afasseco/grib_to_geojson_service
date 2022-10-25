package com.example.demo.service;

import mil.nga.sf.geojson.FeatureCollection;
import mil.nga.sf.geojson.FeatureConverter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class CacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    @Value("${cache.enabled}")
    Boolean cacheEnabled;

    File cacheDir = new File("cache");

    public CacheService() {
        cacheDir.mkdirs();
    }

    public Boolean isEnabled() {
        return cacheEnabled;
    }

    public static String getShortKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes(StandardCharsets.UTF_8));
            return DigestUtils.md5Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public FeatureCollection get(String geoJsonFile) {
        try {
            File file1 = new File(cacheDir, geoJsonFile);
            if (file1.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(file1)) {
                    byte array[] = new byte[(int) file1.length()];
                    IOUtils.readFully(fileInputStream, array);
                    FeatureCollection featureCollection = FeatureConverter.toFeatureCollection(new String(array));
                    return featureCollection;
                }

            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public void save(String shortKey, FeatureCollection featureCollection) {
        try {
            File file = new File(cacheDir, shortKey);
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(FeatureConverter.toStringValue(featureCollection));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
