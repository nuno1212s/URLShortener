package me.nunogneto.serialization.gsonadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {
    @Override
    public void write(JsonWriter jsonWriter, ZonedDateTime zonedDateTime) throws IOException {

        jsonWriter.beginObject();

        jsonWriter.name("time").value(zonedDateTime.toInstant().toEpochMilli());
        jsonWriter.name("zone").value(zonedDateTime.getZone().getId());

        jsonWriter.endObject();
    }

    @Override
    public ZonedDateTime read(JsonReader jsonReader) throws IOException {

        long epochTime = 0;

        String zoneId = null;

        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();

            if (name.equals("time")) {
                epochTime = jsonReader.nextLong();
            } else if (name.equals("zone")) {
                zoneId = jsonReader.nextString();
            }
        }

        jsonReader.endObject();

        if (epochTime == 0 || zoneId == null) {
            throw new IOException("Invalid ZonedDateTime object");
        }

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.of(zoneId));
    }
}
