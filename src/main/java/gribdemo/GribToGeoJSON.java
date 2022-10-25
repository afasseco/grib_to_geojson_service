package gribdemo;

import com.example.demo.HarmonieNeaSFParameter;
import mil.nga.sf.geojson.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.grib.GdsHorizCoordSys;
import ucar.nc2.grib.grib1.*;
import ucar.nc2.grib.grib1.tables.Grib1Customizer;
import ucar.unidata.geoloc.projection.RotatedLatLon;
import ucar.unidata.io.RandomAccessFile;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GribToGeoJSON {
    private static final Logger LOGGER = LoggerFactory.getLogger(GribToGeoJSON.class);

    static Pair<Double, Double> coordinates[] = null;

    static boolean isInbbox(double bbox[], double lon, double lat) {
        return (lon >= bbox[0] && lon <= bbox[2] && lat >= bbox[1] && lat <= bbox[3]);
    }

    public static FeatureCollection main(String filename, String localGribDefinitionFileInEcmwfFormat, double[] bbox, String[] parameterIds, Integer maxPoints) throws IOException {
        List<HashMap<String, Object>> points = null;
        Float lonpole = null;
        Float latpole = null;
        RandomAccessFile raf = new RandomAccessFile(filename, "r");
        Formatter formatter = new Formatter(System.out);
        Grib1RecordScanner reader = new Grib1RecordScanner(raf);
        ucar.nc2.grib.grib1.tables.Grib1ParamTables.addParameterTable(94, 255, 253, localGribDefinitionFileInEcmwfFormat);
        while (reader.hasNext()) {
            // Iterate over each parameter and figure out if this should be included
            ucar.nc2.grib.grib1.Grib1Record gr1 = reader.next();
            Grib1Gds gds = gr1.getGDS();
            Grib1Customizer grib1Customizer = Grib1Customizer.factory(gr1, null);
            Grib1SectionProductDefinition pdSsection = gr1.getPDSsection();

            Grib1Parameter parameter = grib1Customizer.getParameter(pdSsection.getCenter(), pdSsection.getSubCenter(), pdSsection.getTableVersion(), pdSsection.getParameterNumber());
            Grib1ParamLevel plevel = grib1Customizer.getParamLevel(pdSsection);

            //pdSsection.showPds(grib1Customizer, formatter); //Show information about this layer
            HarmonieNeaSFParameter thisParameter = HarmonieNeaSFParameter.getParameter(pdSsection.getParameterNumber(), plevel.getValue1(), plevel.getLevelType());
            boolean shouldIncludeParameter = HarmonieNeaSFParameter.shouldIncludeParameter(parameterIds, thisParameter);
            if (!shouldIncludeParameter) {
                continue;
            }

            GdsHorizCoordSys gdsHorizCoordSys = gds.makeHorizCoordSys();

            float[] data = gr1.readData(raf);

            if( coordinates == null) {
                coordinates = new Pair[data.length];
            }

            int elementNumber = 0;
            boolean pointsAlreadyInitialized = true;
            if( points == null ) {
                points = new ArrayList<>(data.length);
                pointsAlreadyInitialized = false;
            }
            int points_index = 0;

            //Find pole coordinates around which coordinates should be rotated
            lonpole = (float) ((RotatedLatLon) gdsHorizCoordSys.proj).getLonpole();
            latpole = (float) ((RotatedLatLon) gdsHorizCoordSys.proj).findProjectionParameter("grid_south_pole_latitude").getNumericValue();

            LOGGER.info("Handling grib record");
            for (int y = 0; y < gdsHorizCoordSys.ny; y++) {
                double la = gdsHorizCoordSys.starty + y * gdsHorizCoordSys.dy;
                for (int x = 0; x < gdsHorizCoordSys.nx; x++) {
                    float parameterValue = data[elementNumber];
                    double lo = gdsHorizCoordSys.startx + x * gdsHorizCoordSys.dx;

                    Pair<Double, Double> lonLat = coordinates[elementNumber];
                    if(lonLat == null ) {
                        lonLat = GribRotation.getRegularLonLat(lo, la, lonpole, latpole); //Rotate point to correct coordinates
                        coordinates[elementNumber] = lonLat; //Store correct regular coordinates forever, no need to calculate again
                    }
                    boolean shouldInclude = isInbbox(bbox, lonLat.getLeft(), lonLat.getRight());
                    if( shouldInclude ) {
                        HashMap<String, Object> point; //Build a hashmap for each point in grid that stores coordinates and relevant data
                        if (pointsAlreadyInitialized) {
                            point = points.get(points_index);
                        } else { //Does not have element yet, create and save it
                            point = new HashMap<>();
                            point.put("lon", lonLat.getLeft()); //Store coordinates
                            point.put("lat", lonLat.getRight());

                            point.put("rlon", lo); //Store un-rotated coordinates for use in later wind calculations
                            point.put("rlat", la);
                            points.add(point);
                        }
                        point.put(thisParameter.getName(), parameterValue); //Store parameter data
                        points_index++;
                    }
                    elementNumber++;
                }
            }
            LOGGER.info("Handling grib record done");
        }
        raf.close();

        LOGGER.info("Rotating parameters");
        rotateWind(points, lonpole, latpole);

        LOGGER.info("Filtering");
        points = filterAwayTooNearPoints(bbox, points, maxPoints);

        LOGGER.info("To GeoJSON objects");
        FeatureCollection featureCollection = new FeatureCollection();
        for (HashMap<String, Object> point : points) {
            Feature feature = new Feature();
            feature.setGeometry(new Point(new Position((Double) point.get("lon"), (Double) point.get("lat"))));
            featureCollection.addFeature(feature);

            //No longer needed - otherwise these would become properties as well
            point.remove("lon");
            point.remove("lat");
            point.remove("rlon");
            point.remove("rlat");

            //Use rest of hashmap as properties
            feature.setProperties(point);
        }
        LOGGER.info("Returning");
        return featureCollection;
    }

    private static int getFilterPosition(double[] bbox, double increment, HashMap<String, Object> point) {
        Double point_lon = (Double) point.get("lon");
        Double point_lat = (Double) point.get("lat");
        int pos = 0;
        for(double lat = bbox[1]; lat < bbox[3];lat+=increment ) {
            for(double lon = bbox[0]; lon < bbox[2];lon+=increment ) {
                if (point_lon >= lon && point_lat >= lat && point_lon <=lon+increment && point_lat <=lat+increment) {
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    private static List<HashMap<String, Object>> filterAwayTooNearPoints(double[] bbox, List<HashMap<String, Object>> points, int maxPoints) {
        List<HashMap<String, Object>> filteredPoints = new ArrayList<>();
        double totalsize = (bbox[2] - bbox[0]) + (bbox[3] - bbox[1]);
        double increment = totalsize / Math.sqrt(maxPoints);

        Map<Integer, HashMap<String, Object>> collect = points.stream()
                .filter(point ->  isInbbox(bbox, (Double) point.get("lon"), (Double) point.get("lat")))
                .collect(Collectors.groupingBy(point -> getFilterPosition(bbox, increment, point),
                        Collectors.reducing(null, Function.identity(), (first, last) -> last)));
        collect.remove(-1);
        filteredPoints.addAll(collect.values());

        return filteredPoints;
    }

    private static void rotateWind(List<HashMap<String, Object>> points, float lonpole, float latpole) {
        for(HashMap<String, Object> point: points) {
            Double rlon = (Double) point.get("rlon");
            Double rlat = (Double) point.get("rlat");

            Float u = (Float)point.get(HarmonieNeaSFParameter.WIND_DIR_50M_U.getName());
            Float v = (Float)point.get(HarmonieNeaSFParameter.WIND_DIR_50M_V.getName());
            if(u != null && v != null) {
                RotatedWind rotatedWind = GribRotation.rotate_wind(rlon, rlat, u, v, lonpole, latpole);
                point.put(HarmonieNeaSFParameter.WIND_DIR_50M_U.getParameterId(), rotatedWind.direction());

                point.remove(HarmonieNeaSFParameter.WIND_DIR_50M_U.getName());
                point.remove(HarmonieNeaSFParameter.WIND_DIR_50M_V.getName());
            }
        }
    }

}
