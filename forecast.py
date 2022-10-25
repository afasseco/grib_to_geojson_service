import requests, json

# Have to set it to CRS84 for bbox to work
canvas = iface.mapCanvas()
target_crs = QgsCoordinateReferenceSystem()
target_crs.createFromUserInput("OGC:CRS84")
canvas.setDestinationCrs(target_crs)

root = QgsProject.instance().layerTreeRoot()
layer_group = root.insertGroup(0, 'forecast')

url = "http://localhost:8080/collections/harmonieneasf/items"
files = ['HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T150000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T160000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T170000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T180000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T190000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T200000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T210000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T220000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-11T230000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T000000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T010000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T020000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T030000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T040000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T050000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T060000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T070000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T080000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T090000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T100000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T110000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T120000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T130000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T140000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T150000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T160000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T170000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T180000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T190000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T200000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T210000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T220000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-12T230000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T000000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T010000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T020000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T030000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T040000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T050000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T060000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T070000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T080000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T090000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T100000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T110000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T120000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T130000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T140000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T150000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T160000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T170000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T180000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T190000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T200000Z.grib','HARMONIE_NEA_SF_2022-10-11T150000Z_2022-10-13T210000Z.grib']


for file in files:
    print(file)
    extent = iface.mapCanvas().extent()
    bbox = str(extent.xMinimum()) + "," + str(extent.yMinimum()) + "," + str(extent.xMaximum()) + "," + str(extent.yMaximum())
    print('creating vector layer')
    layer = QgsVectorLayer(url + "?file="+file+"&bbox="+bbox+"&parameterId=wind-speed,wind-dir,cloudcover&maxPoints=10000", file)
    layer.setName(file)
    print('creating vector layer done')

    #Set layer tempral properties to be from image date to + 10 minutes
    layer.temporalProperties().setMode(QgsVectorLayerTemporalProperties.ModeFixedTemporalRange)
    d = file[35:53]

    start_time = QDateTime.fromString(d, "yyyy-MM-ddThhmmssZ")
    #HACK: to not make start_time match two ranges
    start_time = start_time.addSecs(1)
    end_time = start_time#QGIS handles range - so no endtime.addSecs(3600)
    time_range = QgsDateTimeRange(start_time, end_time)

    layer.temporalProperties().setFixedTemporalRange(time_range)
    layer.temporalProperties().setIsActive(True)
    
    #use this for wind direction as arrows and wind speed as arrow size
    layer.renderer().symbol().symbolLayer(0).setShape(QgsSimpleMarkerSymbolLayerBase.Arrow)
    layer.renderer().symbol().setDataDefinedAngle(QgsProperty().fromExpression("(\"wind-dir\" + 180) % 360"))
    layer.renderer().symbol().symbolLayer(0).setDataDefinedProperty(QgsSymbolLayer.PropertySize, QgsProperty().fromExpression("\"wind-speed\""))
    layer.renderer().symbol().symbolLayer(0).setColor(QColor.fromRgb(0,162,232)) #Blue colour

    #Use this for cloud covers as white circles with opacity based on cloud cover
    #layer.renderer().symbol().symbolLayer(0).setShape(QgsSimpleMarkerSymbolLayerBase.Circle)
    #layer.renderer().symbol().symbolLayer(0).setSize(5)
    #layer.renderer().symbol().symbolLayer(0).setColor(QColor.fromRgb(255,255,255)) #White colour
    #layer.renderer().symbol().setDataDefinedProperty(QgsSymbol.PropertyOpacity, QgsProperty().fromExpression("\"cloudcover\" * 100"))
    layer.triggerRepaint()

    project = QgsProject.instance()
    project.addMapLayer(layer, addToLegend=False)
    layer_group.insertLayer(-1, layer)
    
print("Done")
