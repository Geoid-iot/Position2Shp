package com.example.position2shp.Settings;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.position2shp.Map.ReferenceSystems;
import com.example.position2shp.Map.TrackingColour;

public class Settings implements Parcelable {
    public String backgroundShpFile;
    public String positionTrackingShapefilePath;
    public int pointColor;
    public int lineColor;
    public int polygonFillColour;
    public int polygonLineColour;
    public int refSystem;
    public double trackingSensitivity;
    public String externalFilesDir;
    public String projFilesDir;
    public boolean createNewShapefile;

    public boolean automaticTracking;

    public Settings()
    {
        super();
        backgroundShpFile = "";
        positionTrackingShapefilePath = "";
        pointColor = TrackingColour.BLUE.getNumVal();
        lineColor = TrackingColour.BLUE.getNumVal();
        polygonFillColour = TrackingColour.TRANSPERENT.getNumVal();
        polygonLineColour = TrackingColour.BLUE.getNumVal();
        refSystem = ReferenceSystems.WGS84.ordinal();
        trackingSensitivity = 1.5;
        externalFilesDir = "";
        projFilesDir = "";
        createNewShapefile = true;
        automaticTracking = true;
    }

    public Settings(Settings s)
    {
        super();
        backgroundShpFile = s.backgroundShpFile;
        positionTrackingShapefilePath = s.positionTrackingShapefilePath;
        pointColor = s.pointColor;
        lineColor = s.lineColor;
        polygonFillColour = s.polygonFillColour;
        polygonLineColour = s.polygonLineColour;
        refSystem = s.refSystem;
        trackingSensitivity = s.trackingSensitivity;
        externalFilesDir = s.externalFilesDir;
        projFilesDir = s.projFilesDir;
        createNewShapefile = s.createNewShapefile;
        automaticTracking = s.automaticTracking;
    }
    protected Settings(Parcel in) {
        backgroundShpFile = in.readString();
        positionTrackingShapefilePath = in.readString();
        pointColor = in.readInt();
        lineColor = in.readInt();
        polygonFillColour = in.readInt();
        polygonLineColour = in.readInt();
        refSystem = in.readInt();
        trackingSensitivity = in.readDouble();
        externalFilesDir = in.readString();
        projFilesDir = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createNewShapefile = in.readBoolean();
            automaticTracking = in.readBoolean();
        }
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
        dest.writeString(positionTrackingShapefilePath);
        dest.writeInt(pointColor);
        dest.writeInt(lineColor);
        dest.writeInt(polygonFillColour);
        dest.writeInt(polygonLineColour);
        dest.writeInt(refSystem);
        dest.writeDouble(trackingSensitivity);
        dest.writeString(externalFilesDir);
        dest.writeString(projFilesDir);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(createNewShapefile);
            dest.writeBoolean(automaticTracking);
        }
    }
}
