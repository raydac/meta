/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.meta.common.utils;

import com.igormaznitsa.meta.common.annotations.NonNull;
import com.igormaznitsa.meta.common.annotations.Nullable;
import com.igormaznitsa.meta.common.annotations.ThreadSafe;
import com.igormaznitsa.meta.common.annotations.Weight;
import com.igormaznitsa.meta.common.global.special.GlobalErrorListeners;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Auxiliary methods for IO operations.
 *
 * @since 1.0
 */
@ThreadSafe
public final class IOUtils {

  private IOUtils () {
  }

  /**
   * Pack some binary data.
   *
   * @param data data to be packed
   * @return packed data as byte array
   * @since 1.0
   */
  @NonNull
  @Weight (Weight.Unit.VARIABLE)
  public static byte[] packData (@NonNull final byte[] data) {
    final Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
    compressor.setInput(Assertions.assertNotNull(data));
    compressor.finish();
    final ByteArrayOutputStream resultData = new ByteArrayOutputStream(data.length + 100);

    final byte[] buffer = new byte[1024];
    while (!compressor.finished()) {
      resultData.write(buffer, 0, compressor.deflate(buffer));
    }

    return resultData.toByteArray();
  }

  /**
   * Unpack binary data packed by the packData method.
   *
   * @param data packed data array
   * @return unpacked byte array
   * @throws IllegalArgumentException it will be thrown if the data has wrong
   * format, global error listeners will be also notified
   * @see #packData(byte[])
   * @since 1.0
   */
  @NonNull
  @Weight (Weight.Unit.VARIABLE)
  public static byte[] unpackData (@NonNull final byte[] data) {
    final Inflater decompressor = new Inflater();
    decompressor.setInput(Assertions.assertNotNull(data));
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream(data.length * 2);
    final byte[] buffer = new byte[1024];
    try {
      while (!decompressor.finished()) {
        outStream.write(buffer, 0, decompressor.inflate(buffer));
      }
      return outStream.toByteArray();
    }
    catch (DataFormatException ex) {
      GlobalErrorListeners.fireError("Can't unpack data for wrong format", ex);
      throw new IllegalArgumentException("Wrong formatted data", ex);
    }
  }

  /**
   * Closing quetly any closeable object. Any exception will be caught (but
   * global error listeners will be notified)
   *
   * @param closeable object to be closed quetly
   * @since 1.0
   */
  @Weight (Weight.Unit.LIGHT)
  public static void closeQuetly (@Nullable final Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      }
      catch (Exception ex) {
        GlobalErrorListeners.fireError("Exception in closeQuetly", ex);
      }
    }
  }
}