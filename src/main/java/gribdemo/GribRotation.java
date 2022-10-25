package gribdemo;

import org.apache.commons.lang3.tuple.Pair;

public class GribRotation {
    //Rotation code converted to Java from: https://confluence.govcloud.dk/pages/viewpage.action?pageId=76153193
    static Pair<Double, Double> getRegularLonLat(double rot_lon, double rot_lat, float southpole_lon, float southpole_lat) {
        double to_rad = Math.PI / 180.0;
        double to_deg = 1.0 / to_rad;
        double sin_y_cen = Math.sin(to_rad * (southpole_lat + 90.0));
        double cos_y_cen = Math.cos(to_rad * (southpole_lat + 90.0));

        double sin_x_rot = Math.sin(to_rad * rot_lon);
        double cos_x_rot = Math.cos(to_rad * rot_lon);
        double sin_y_rot = Math.sin(to_rad * rot_lat);
        double cos_y_rot = Math.cos(to_rad * rot_lat);
        double sin_y_reg = cos_y_cen * sin_y_rot + sin_y_cen * cos_y_rot * cos_x_rot;
        if (sin_y_reg < -1.0) sin_y_reg = -1.0;
        if (sin_y_reg > 1.0) sin_y_reg = 1.0;

        double reg_lat = (float) to_deg * Math.asin(sin_y_reg);

        double cos_y_reg = Math.cos(reg_lat * to_rad);
        double cos_lon_rad = (cos_y_cen * cos_y_rot * cos_x_rot - sin_y_cen * sin_y_rot) / cos_y_reg;
        if (cos_lon_rad < -1.0) cos_lon_rad = -1.0;
        if (cos_lon_rad > 1.0) cos_lon_rad = 1.0;
        double sin_lon_rad = cos_y_rot * sin_x_rot / cos_y_reg;
        double lon_rad = Math.acos(cos_lon_rad);
        if (sin_lon_rad < 0.0) lon_rad = -lon_rad;

        double reg_lon = to_deg * lon_rad + southpole_lon;
        Pair<Double, Double> regularLonLat = Pair.of(reg_lon, reg_lat);
        return regularLonLat;
    }

    static RotatedWind rotate_wind(double rot_lon, double rot_lat,
                                   double u_in, double v_in,
                                   float southpole_lon, float southpole_lat)
        /* Given either a point in the regular grid (set `rot_lat' <= -999.0)
         * or a point in the rotated grid, calculate the corresponding point
         * in the opposite grid, change the (u, v)-vector from rotated to
         * regular grid and calculate the wind force (`*strength') and the
         * wind direction in the regular grid. `southpole_lat' and
         * `southpole_lon' defines the coordinate of the southpole in
         * the roated grid */
    {

        /* Find the missing point, whether is is the rotated or the regular */
        //if (rot_lat <= -999.0) { //Do not need this conversion - not converted into Java
            //reg2rot(reg_lat, reg_lon, rot_lat, rot_lon, southpole_lat, southpole_lon);
        //} else {
        Pair<Double, Double> regularLonLat = getRegularLonLat(rot_lon, rot_lat, southpole_lon, southpole_lat);

        double reg_lon = regularLonLat.getLeft();
        double reg_lat = regularLonLat.getRight();
        //}

        /* Calculate the wind strength */
        double strength = Math.sqrt(u_in * u_in + v_in * v_in);

        /* Add a small distance in the direction of the wind to the rotated
         * grid point, changing the distance into degrees */
        double rot_lat2 = (rot_lat) + 0.1*v_in/(strength);
        double clat = Math.cos((rot_lat)*Math.PI/180.0);
        if (0.0001 > clat && clat > -0.0001) {
            throw new RuntimeException("Internal error: Too close to pole to calculate rotated wind");
        }
        double rot_lon2 = (rot_lon) + 0.1*u_in/(strength * clat);

        /* Translate new rotated grid point to regular grid */
        Pair<Double, Double> regularLonLat2 = getRegularLonLat(rot_lon2, rot_lat2, southpole_lon, southpole_lat);
        double reg_lon2 = regularLonLat2.getLeft();
        double reg_lat2 = regularLonLat2.getRight();

        /* Transform offset in lat-lon to offset in x-y */
        clat = Math.cos((reg_lat)*Math.PI/180.0);
        double dx = clat*(reg_lon2 - reg_lon);

        /* Calculate the direction of the wind vector in the regular grid */
        double direc = Math.atan2(reg_lat2 - reg_lat, dx);

        /* Regular direction in degrees */
        double direction = 630.0 - direc*180.0 / Math.PI;
        while (direction > 360.0) {
            direction -= 360.0;
        }

        double u_out = Math.cos(direc) * (strength);
        double v_out = Math.sin(direc) * (strength);

        return new RotatedWind(u_out, v_out, strength, direction);
    }
}
