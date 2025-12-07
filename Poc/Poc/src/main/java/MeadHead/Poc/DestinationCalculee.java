package MeadHead.Poc;

public class DestinationCalculee {
private PositionGPS destinationPosition;
private String destinationAdresse;
private long distanceMetres;
private long dureeSecondes;
private boolean trajetValide;

    public DestinationCalculee(PositionGPS destinationPosition, String destinationAdresse, long distanceMetres, long dureeSecondes, boolean trajetValide) {
        this.destinationPosition = destinationPosition;
        this.destinationAdresse = destinationAdresse;
        this.distanceMetres = distanceMetres;
        this.dureeSecondes = dureeSecondes;
        this.trajetValide = trajetValide;

    }

    public PositionGPS getDestinationPosition() {
        return destinationPosition;
    }
    public String getDestinationAdresse() {
        return destinationAdresse;
    }
    public long getDistanceMetres() {
        return distanceMetres;
    }
    public long getDureeSecondes() {
        return dureeSecondes;
    }
    public boolean isTrajetValide() {
        return trajetValide;
    }
}
