package com.example.demo.api;

import com.example.demo.service.CacheService;
import com.example.demo.service.GribDefinitionService;
import com.example.demo.service.ModelService;
import gribdemo.GribToGeoJSON;
import mil.nga.sf.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@RestController
public class HarmonieNeaSfApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarmonieNeaSfApi.class);

    @Autowired
    CacheService cacheService;

    @Autowired
    ModelService modelService;

    @Autowired
    GribDefinitionService gribDefinitionService;

    @GetMapping(value = "/collections/harmonieneasf/items")
    public FeatureCollection getForecast(@RequestParam String file, @RequestParam String bbox[], @RequestParam String parameterId,
                                         @RequestParam() Integer maxPoints, //Max points to return - for use in quick filtering
                                         HttpServletRequest request) throws IOException {
        if (cacheService.isEnabled()) {
            String requestUrlAndParameters = request.getRequestURI() + "?" + request.getQueryString();
            String geoJsonFile = CacheService.getShortKey(requestUrlAndParameters);
            FeatureCollection featureCollection = cacheService.get(geoJsonFile);
            if (featureCollection != null) {
                return featureCollection;
            }
        }
        double[] bboxAsDoubles = new double[]{Double.parseDouble(bbox[0]), Double.parseDouble(bbox[1]), Double.parseDouble(bbox[2]), Double.parseDouble(bbox[3])};

        File modelFile = modelService.getModelFile(file);
        if (modelFile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found");
        }

        String[] parameterIdArray;
        if (parameterId == null) {
            parameterIdArray = new String[0];
        } else {
            parameterIdArray = parameterId.split(",");
        }

        FeatureCollection featureCollection = GribToGeoJSON.main(modelFile.getAbsolutePath(), gribDefinitionService.getGribDefinition().getAbsolutePath(), bboxAsDoubles, parameterIdArray, maxPoints);

        if (cacheService.isEnabled()) {
            String requestUrlAndParameters = request.getRequestURI() + "?" + request.getQueryString();
            String geoJsonFile = CacheService.getShortKey(requestUrlAndParameters);
            cacheService.save(geoJsonFile, featureCollection);
        }
        return featureCollection;
    }
}
