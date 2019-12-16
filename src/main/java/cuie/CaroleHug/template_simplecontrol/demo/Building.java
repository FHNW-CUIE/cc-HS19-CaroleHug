package cuie.CaroleHug.template_simplecontrol.demo;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class Building {

        private final IntegerProperty id = new SimpleIntegerProperty();
        private final IntegerProperty rank = new SimpleIntegerProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty city = new SimpleStringProperty();
        private final StringProperty country = new SimpleStringProperty();
        private final FloatProperty heightM = new SimpleFloatProperty();
        private final FloatProperty heightFT = new SimpleFloatProperty();
        private final IntegerProperty floors = new SimpleIntegerProperty();
        private final IntegerProperty build = new SimpleIntegerProperty();
        private final StringProperty architect = new SimpleStringProperty();
        private final StringProperty architectualStyle = new SimpleStringProperty();
        private final FloatProperty cost = new SimpleFloatProperty();
        private final StringProperty material = new SimpleStringProperty();
        private final StringProperty longitude = new SimpleStringProperty();
        private final StringProperty latitude = new SimpleStringProperty();
        private final StringProperty imageUrl = new SimpleStringProperty();

        private static final Map<String, Image> flagImage = new HashMap<>();

        public Building(){

        }


    public Building(String[] line) {
            setId(Integer.valueOf(line[0]));
            setRank(Integer.valueOf(line[1]));
            setName(line[2]);
            setCity(line[3]);
            setCountry(line[4]);
            setHeightM(Float.valueOf(line[5]));
            setHeightFT(Float.valueOf(line[6]));
            setFloors(Integer.valueOf(line[7]));
            setBuild(Integer.valueOf(line[8]));
            setArchitect(line[9]);
            setArchitectualStyle(line[10]);
            setCost(Float.valueOf(line[11]));
            setMaterial(line[12]);
            setLongitude(line[13]);
            //setLatitude(line[14]);
            //setImageUrl(line[15]);
            System.out.println("test");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Building resultat = (Building) o;

            return getId()==(resultat.getId());
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        public String infoAsLine() {
            return String.join(";",
                    Integer.toString(getId()),
                    getName(),
                    getCity(),
                    getCountry(),
                    Float.toString(getHeightM()),
                    Float.toString(getHeightFT()),
                    Integer.toString(getFloors()),
                    Integer.toString(getBuild()),
                    getArchitect(),
                    getArchitectualStyle(),
                    Float.toString(getCost()),
                    getMaterial(),
                  //  Float.toString(getLongitude()),
                  //  Float.toString(getLatitude()),
                    getImageUrl());
        }

        @Override
        public String toString() {
            return infoAsLine();
        }



    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getRank() {
        return rank.get();
    }

    public IntegerProperty rankProperty() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank.set(rank);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getCity() {
        return city.get();
    }

    public StringProperty cityProperty() {
        return city;
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    public String getCountry() {
        return country.get();
    }

    public StringProperty countryProperty() {
        return country;
    }

    public void setCountry(String country) {
        this.country.set(country);
    }

    public float getHeightM() {
        return heightM.get();
    }

    public FloatProperty heightMProperty() {
        return heightM;
    }

    public void setHeightM(float heightM) {
        this.heightM.set(heightM);
    }

    public float getHeightFT() {
        return heightFT.get();
    }

    public FloatProperty heightFTProperty() {
        return heightFT;
    }

    public void setHeightFT(float heightFT) {
        this.heightFT.set(heightFT);
    }

    public int getFloors() {
        return floors.get();
    }

    public IntegerProperty floorsProperty() {
        return floors;
    }

    public void setFloors(int floors) {
        this.floors.set(floors);
    }

    public int getBuild() {
        return build.get();
    }

    public IntegerProperty buildProperty() {
        return build;
    }

    public void setBuild(int build) {
        this.build.set(build);
    }

    public String getArchitect() {
        return architect.get();
    }

    public StringProperty architectProperty() {
        return architect;
    }

    public void setArchitect(String architect) {
        this.architect.set(architect);
    }

    public String getArchitectualStyle() {
        return architectualStyle.get();
    }

    public StringProperty architectualStyleProperty() {
        return architectualStyle;
    }

    public void setArchitectualStyle(String architectualStyle) {
        this.architectualStyle.set(architectualStyle);
    }

    public float getCost() {
        return cost.get();
    }

    public FloatProperty costProperty() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost.set(cost);
    }

    public String getMaterial() {
        return material.get();
    }

    public StringProperty materialProperty() {
        return material;
    }

    public void setMaterial(String material) {
        this.material.set(material);
    }

    public String getImageUrl() {
        return imageUrl.get();
    }

    public StringProperty imageUrlProperty() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl.set(imageUrl);
    }


    public String getLongitude() {
        return longitude.get();
    }

    public StringProperty longitudeProperty() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude.set(longitude);
    }

    public String getLatitude() {
        return latitude.get();
    }

    public StringProperty latitudeProperty() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude.set(latitude);
    }
}
