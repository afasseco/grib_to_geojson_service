package com.example.demo;

public enum HarmonieNeaSFParameter {

    CLOUDCOVER("cloudcover", 71, 0, 105),
    DEW_TEMPERATURE("dew-temperature", 17, 2, 105),
    LAND_PERCENT("land-percent", 81, 0, 105),
    PRESSURE_SEALEVEL("pressure-sealevel", 1, 0, 103),
    TEMPERATURE_50M("temperature-50m",11, 50, 105),
    WIND_DIR("wind-dir", 31, 10, 105),
    WIND_DIR_50M_U("wind-dir-50m", "wind-dir-50m-u", 33, 50, 105),
    WIND_DIR_50M_V("wind-dir-50m", "wind-dir-50m-v", 34, 50, 105),
    WIND_SPEED("wind-speed",32, 10, 105);

    private final String parameterId;
    private final String name;
    private final int parameterNumber;
    private final float levelValue1;
    private final int levelType;

    HarmonieNeaSFParameter(String name, int parameterNumber, float levelValue1, int levelType) {
        this.parameterId = null;
        this.name = name;
        this.parameterNumber = parameterNumber;
        this.levelValue1 = levelValue1;
        this.levelType = levelType;
    }

    HarmonieNeaSFParameter(String parameterId, String name, int parameterNumber, float levelValue1, int levelType) {
        this.parameterId = parameterId;
        this.name = name;
        this.parameterNumber = parameterNumber;
        this.levelValue1 = levelValue1;
        this.levelType = levelType;
    }

    public String getParameterId() {
        return parameterId;
    }

    public String getName() {
        return name;
    }

    public static HarmonieNeaSFParameter getParameter(String parameterId) {
        for( HarmonieNeaSFParameter harmonieNeaSFParameter : values()) {
            if( harmonieNeaSFParameter.name.equals(parameterId)) {
                return harmonieNeaSFParameter;
            }
        }
        return null;
    }

    public static HarmonieNeaSFParameter getParameter(int parameterNumber, float levelValue1, int levelType) {
        for( HarmonieNeaSFParameter harmonieNeaSFParameter : values()) {
            if( parameterNumber == harmonieNeaSFParameter.parameterNumber &&
                    levelValue1 == harmonieNeaSFParameter.levelValue1 &&
                    levelType == harmonieNeaSFParameter.levelType) {
                return harmonieNeaSFParameter;
            }
        }
        return null;
    }

    public static boolean shouldIncludeParameter(String[] parameterIds, HarmonieNeaSFParameter harmonieNeaSFParameter) {
        if( harmonieNeaSFParameter == null) {
            return false;
        }
        for( String parameterId: parameterIds) {
            if( harmonieNeaSFParameter.parameterId != null && harmonieNeaSFParameter.parameterId.equals(parameterId)) {
                return true;
            }
            if( getParameter(parameterId) == harmonieNeaSFParameter ) {
                return true;
            }
        }

        return false;
    }
}
