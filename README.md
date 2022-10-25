# grib_to_geojson_service
Example on how to use DMIs grib files packaged as a service. Demonstrates both coordinate rotation and wind rotation.

## Running the service

Two properties need to be set before running the service, so create a application-local.properties file in 
src/main/resources/

with two properties:

    forecast.apikey=your api key for the forecast API - get one here: https://confluence.govcloud.dk/display/FDAPI/Getting+Started
    local_table_definition_file=<path to the dmi grib definition file>/dmi_grib_definitions/grib1/2.94.253.table

dmi grib definition file can be found attached to this page: https://confluence.govcloud.dk/pages/viewpage.action?pageId=76153348

With application-local.properties in place Run the Application.class with arguments --spring.profiles.active=local and service should start up listening on port 8080.
You can also just put these two properties into the existing application.properties file.

When service is running it can be called like so:

    GET http://localhost:8080/collections/harmonieneasf/items?bbox=11,55,12,56&file=HARMONIE_NEA_SF_2022-10-08T000000Z_2022-10-08T010000Z.grib&parameterId=wind-speed,wind-dir,cloudcover,temperature-50m,wind-dir-50m&maxPoints=20

If the file is not already in the service it will be downloaded from the forecast API (using the api-key configured above).

## Displaying data in QGIS

Edit the [forecast.py](forecast.py) to use the relevant forecast files and In QGIS run the edited forecast.py file using:

    exec(Path('<path>/forecast.py').read_text())

This will call the service for each file to get the forecast as GeoJSON and each layer timecoded so the QGIS Temporal Controller can be used like so:

![](QGIS-wind.gif)

