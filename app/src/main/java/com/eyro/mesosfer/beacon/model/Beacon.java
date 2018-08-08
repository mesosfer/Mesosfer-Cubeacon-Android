package com.eyro.mesosfer.beacon.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Created by Eyro on 12/20/16.
 */

public class Beacon implements Parcelable {
  private String identifier;
  private UUID proximityUUID;
  private int major;
  private int minor;

  public Beacon() {
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public int getMajor() {
    return major;
  }

  public void setMajor(int major) {
    this.major = major;
  }

  public int getMinor() {
    return minor;
  }

  public void setMinor(int minor) {
    this.minor = minor;
  }

  public UUID getProximityUUID() {
    return proximityUUID;
  }

  public void setProximityUUID(UUID proximityUUID) {
    this.proximityUUID = proximityUUID;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.identifier);
    dest.writeSerializable(this.proximityUUID);
    dest.writeInt(this.major);
    dest.writeInt(this.minor);
  }

  private Beacon(Parcel in) {
    this.identifier = in.readString();
    this.proximityUUID = (UUID) in.readSerializable();
    this.major = in.readInt();
    this.minor = in.readInt();
  }

  public static final Parcelable.Creator<Beacon> CREATOR = new Parcelable.Creator<Beacon>() {
    @Override
    public Beacon createFromParcel(Parcel source) {
      return new Beacon(source);
    }

    @Override
    public Beacon[] newArray(int size) {
      return new Beacon[size];
    }
  };
}
