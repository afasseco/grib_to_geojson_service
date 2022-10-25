package gribdemo;

public record RotatedWind(
    double u_out,
    double v_out,
    double strength,
    double direction
) {
}
