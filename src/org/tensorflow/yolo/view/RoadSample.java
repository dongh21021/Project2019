package org.tensorflow.yolo.view;

class RoadSample {
    private String no;
    private String lat;
    private String lon;
    private String lane;
    private String width;
    private String length;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "RoadSample{" +
                "no='" + no + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", lane='" + lane + '\'' +
                ", width='" + width + '\'' +
                ", length='" + length + '\'' +
                '}';
    }
}

