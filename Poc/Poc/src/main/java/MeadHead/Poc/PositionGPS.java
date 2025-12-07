package MeadHead.Poc;

public class PositionGPS {
    private double latitude;
    private double longitude;

    public PositionGPS(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return this.latitude + "," + this.longitude;
    }

}
