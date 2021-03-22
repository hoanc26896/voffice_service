/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

/**
 *
 * @author thanght6
 */
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ErrorCodeEnumAdapterTypeAdapter<T> extends TypeAdapter<T> {

    @Override
    public T read(JsonReader arg0) throws IOException {
        return null;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        ErrorCode errorCode = (ErrorCode) value;
        out.beginObject();
        out.name("errorCode");
        out.value(errorCode.getErrorCode());
        out.name("message");
        out.value(errorCode.getMessage());
        out.endObject();

    }
}