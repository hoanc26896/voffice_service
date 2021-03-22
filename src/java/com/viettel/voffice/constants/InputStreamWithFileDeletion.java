/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author datnv5
 */
public class InputStreamWithFileDeletion extends FileInputStream {
    File f;

    public InputStreamWithFileDeletion(File file) throws FileNotFoundException {
        super(file);
        f = file;
    }

    @Override
    public void close() throws IOException {
        super.close();
        f.delete();
    }
}
