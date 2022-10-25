package com.example.demo.service;

import gribdemo.model.forecastservice.Item;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Service
public class ModelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

    @Autowired
    RestTemplate restTemplate;

    @Value("${forecast.url}")
    String forecastUrl;

    @Value("${forecast.apikey}")
    String forecastApikey;

    File modelDir;
    File tempDir;

    public ModelService() {
        modelDir = new File("modeldir");
        modelDir.mkdirs();

        tempDir = new File(modelDir, "temp");
        tempDir.mkdirs();
    }

    public File getModelFile(String file) {
        File modelFile = new File(modelDir, file);
        if (!modelFile.exists()) {
            modelFile = download(file);
        }
        return modelFile;
    }

    private File download(String file) {
        try {
            Item item = restTemplate.getForObject(forecastUrl + "/" + file + "?api-key=" + forecastApikey, Item.class);
            URL url = new URL(item.asset.data.href);
            FileUtils.copyURLToFile(url, new File(tempDir, file));
            FileUtils.moveFile(new File(tempDir, file), new File(modelDir, file));
            return new File(modelDir, file);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        } catch (HttpClientErrorException e) {
            LOGGER.info(e.getMessage(), e);
            if (e.getRawStatusCode() != 404) { //404 is ok
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
