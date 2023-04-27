package com.example.position2shp.Settings;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.position2shp.Map.ReferenceSystems;
import com.example.position2shp.Map.TrackingColour;

public class Settings implements Parcelable {

    public String backgroundShpFile;
    public int pointColor;
    public int lineColor;
    public int polygonColor;
    public int refSystem;
    public double trackingSensitivity;
    public String externalFilesDir;
    public String projFilesDir;

    public Settings()
    {
        super();
        backgroundShpFile = "";
        pointColor = TrackingColour.BLUE.ordinal();
        lineColor = TrackingColour.BLUE.ordinal();
        polygonColor = TrackingColour.BLUE.ordinal();
        refSystem = ReferenceSystems.WGS84.ordinal();
        trackingSensitivity = 1.5;
        externalFilesDir = "";
        projFilesDir = "";
    }

    protected Settings(Parcel in) {
        backgroundShpFile = in.readString();
        pointColor = in.readInt();
        lineColor = in.readInt();
        polygonColor = in.readInt();
        refSystem = in.readInt();
        trackingSensitivity = in.readDouble();
        externalFilesDir = in.readString();
        projFilesDir = in.readString();
    }

    public Settings setMapSettings(Settings set)
    {
        backgroundShpFile = set.backgroundShpFile;
        pointColor = set.pointColor;
        lineColor = set.lineColor;
        polygonColor = set.polygonColor;

        return this;
    }

    public Settings setTrackingSettings(Settings set)
    {
        refSystem = set.refSystem;
        trackingSensitivity = set.trackingSensitivity;

        return this;
    }

    public static final Creator<Settings> CREATOR = new Creator<Settings>() {
        @Override
        public Settings createFromParcel(Parcel in) {
            return new Settings(in);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(backgroundShpFile);
        dest.writeInt(pointColor);
        dest.writeInt(lineColor);
        dest.writeInt(polygonColor);
        dest.writeInt(refSystem);
        dest.writeDouble(trackingSensitivity);
        dest.writeString(externalFilesDir);
        dest.writeString(projFilesDir);
    }
}
