/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.integration.transports.netty;

import org.hornetq.core.remoting.spi.MessagingBuffer;
import org.hornetq.utils.DataConstants;
import org.hornetq.utils.SimpleString;
import org.hornetq.utils.UTF8Util;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Wraps Netty {@link ChannelBuffer} with {@link MessagingBuffer}.
 * Because there's neither {@code position()} nor {@code limit()} in a Netty
 * buffer.  {@link ChannelBuffer#readerIndex()} and {@link ChannelBuffer#writerIndex()}
 * are used as {@code position} and {@code limit} of the buffer respectively
 * instead.
 *
 * @author <a href="mailto:tlee@redhat.com">Trustin Lee</a>
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 *
 * @version $Rev$, $Date$
 */
public class ChannelBufferWrapper implements MessagingBuffer
{

   private final ChannelBuffer buffer;

   /**
    * @param buffer
    */
   public ChannelBufferWrapper(final ChannelBuffer buffer)
   {
      super();
      this.buffer = buffer;
   }

   public int capacity()
   {
      return buffer.capacity();
   }

   public void clear()
   {
      buffer.clear();
   }

   public boolean readable()
   {
      return buffer.readable();
   }

   public int readableBytes()
   {
      return buffer.readableBytes();
   }

   public byte readByte()
   {
      return buffer.readByte();
   }

   public void readBytes(final byte[] dst, final int dstIndex, final int length)
   {
      buffer.readBytes(dst, dstIndex, length);
   }

   public void readBytes(final byte[] dst)
   {
      buffer.readBytes(dst);
   }

   public int readerIndex()
   {
      return buffer.readerIndex();
   }

   public void readerIndex(final int readerIndex)
   {
      buffer.readerIndex(readerIndex);
   }

   public int readInt()
   {
      return buffer.readInt();
   }

   public long readLong()
   {
      return buffer.readLong();
   }

   public short readShort()
   {
      return buffer.readShort();
   }

   public short readUnsignedByte()
   {
      return buffer.readUnsignedByte();
   }

   public int readUnsignedShort()
   {
      return buffer.readUnsignedShort();
   }

   public void resetReaderIndex()
   {
      buffer.resetReaderIndex();
   }

   public void resetWriterIndex()
   {
      buffer.resetWriterIndex();
   }

   public void setIndex(final int readerIndex, final int writerIndex)
   {
      buffer.setIndex(readerIndex, writerIndex);
   }

   public void setInt(final int index, final int value)
   {
      buffer.setInt(index, value);
   }

   public boolean writable()
   {
      return buffer.writable();
   }

   public int writableBytes()
   {
      return buffer.writableBytes();
   }

   public void writeByte(final byte value)
   {
      buffer.writeByte(value);
   }

   public void writeBytes(final byte[] src, final int srcIndex, final int length)
   {
      buffer.writeBytes(src, srcIndex, length);
   }
   
   public void writeBytes(MessagingBuffer src, int srcIndex, int length)
   {
      byte bytes[] = new byte[length];
      src.readBytes(bytes, srcIndex, length);
      this.writeBytes(bytes);      
   }

   public void writeBytes(final byte[] src)
   {
      buffer.writeBytes(src);
   }

   public void writeInt(final int value)
   {
      buffer.writeInt(value);
   }

   public void writeLong(final long value)
   {
      buffer.writeLong(value);
   }

   public int writerIndex()
   {
      return buffer.writerIndex();
   }

   public void writerIndex(final int writerIndex)
   {
      buffer.writerIndex(writerIndex);
   }

   public void writeShort(final short value)
   {
      buffer.writeShort(value);
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#array()
    */
   public byte[] array()
   {
      return buffer.toByteBuffer().array();
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readBoolean()
    */
   public boolean readBoolean()
   {
      return readByte() != 0;
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readChar()
    */
   public char readChar()
   {
      return (char)readShort();
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readDouble()
    */
   public double readDouble()
   {
      return Double.longBitsToDouble(readLong());
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readFloat()
    */
   public float readFloat()
   {
      return Float.intBitsToFloat(readInt());
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readNullableSimpleString()
    */
   public SimpleString readNullableSimpleString()
   {
      int b = readByte();
      if (b == DataConstants.NULL)
      {
         return null;
      }
      else
      {
         return readSimpleString();
      }
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readNullableString()
    */
   public String readNullableString()
   {
      int b = readByte();
      if (b == DataConstants.NULL)
      {
         return null;
      }
      else
      {
         return readString();
      }
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readSimpleString()
    */
   public SimpleString readSimpleString()
   {
      int len = readInt();
      byte[] data = new byte[len];
      readBytes(data);
      return new SimpleString(data);
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readString()
    */
   public String readString()
   {
      int len = readInt();
      char[] chars = new char[len];
      for (int i = 0; i < len; i++)
      {
         chars[i] = readChar();
      }
      return new String(chars);
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#readUTF()
    */
   public String readUTF() throws Exception
   {
      return UTF8Util.readUTF(this);
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeBoolean(boolean)
    */
   public void writeBoolean(final boolean val)
   {
      writeByte((byte)(val ? -1 : 0));
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeChar(char)
    */
   public void writeChar(final char val)
   {
      writeShort((short)val);
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeDouble(double)
    */
   public void writeDouble(final double val)
   {
      writeLong(Double.doubleToLongBits(val));

   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeFloat(float)
    */
   public void writeFloat(final float val)
   {
      writeInt(Float.floatToIntBits(val));

   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeNullableSimpleString(org.hornetq.util.SimpleString)
    */
   public void writeNullableSimpleString(final SimpleString val)
   {
      if (val == null)
      {
         writeByte(DataConstants.NULL);
      }
      else
      {
         writeByte(DataConstants.NOT_NULL);
         writeSimpleString(val);
      }
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeNullableString(java.lang.String)
    */
   public void writeNullableString(final String val)
   {
      if (val == null)
      {
         writeByte(DataConstants.NULL);
      }
      else
      {
         writeByte(DataConstants.NOT_NULL);
         writeString(val);
      }
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeSimpleString(org.hornetq.util.SimpleString)
    */
   public void writeSimpleString(final SimpleString val)
   {
      byte[] data = val.getData();
      writeInt(data.length);
      writeBytes(data);
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeString(java.lang.String)
    */
   public void writeString(final String val)
   {
      writeInt(val.length());
      for (int i = 0; i < val.length(); i++)
      {
         writeShort((short)val.charAt(i));
      }
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#writeUTF(java.lang.String)
    */
   public void writeUTF(final String utf) throws Exception
   {
      UTF8Util.saveUTF(this, utf);
   }

   /* (non-Javadoc)
    * @see org.hornetq.core.remoting.spi.MessagingBuffer#getUnderlyingBuffer()
    */
   public Object getUnderlyingBuffer()
   {
      return buffer;
   }

}