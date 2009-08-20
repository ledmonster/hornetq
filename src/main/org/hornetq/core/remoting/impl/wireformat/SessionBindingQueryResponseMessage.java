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

package org.hornetq.core.remoting.impl.wireformat;

import java.util.ArrayList;
import java.util.List;

import org.hornetq.core.remoting.spi.MessagingBuffer;
import org.hornetq.utils.DataConstants;
import org.hornetq.utils.SimpleString;

/**
 * 
 * A SessionBindingQueryResponseMessage
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class SessionBindingQueryResponseMessage extends PacketImpl
{
   private boolean exists;

   private List<SimpleString> queueNames;

   public SessionBindingQueryResponseMessage(final boolean exists, final List<SimpleString> queueNames)
   {
      super(SESS_BINDINGQUERY_RESP);

      this.exists = exists;

      this.queueNames = queueNames;
   }

   public SessionBindingQueryResponseMessage()
   {
      super(SESS_BINDINGQUERY_RESP);
   }

   @Override
   public boolean isResponse()
   {
      return true;
   }

   public boolean isExists()
   {
      return exists;
   }

   public List<SimpleString> getQueueNames()
   {
      return queueNames;
   }

   @Override
   public void encodeBody(final MessagingBuffer buffer)
   {
      buffer.writeBoolean(exists);
      buffer.writeInt(queueNames.size());
      for (SimpleString queueName : queueNames)
      {
         buffer.writeSimpleString(queueName);
      }
   }

   @Override
   public void decodeBody(final MessagingBuffer buffer)
   {
      exists = buffer.readBoolean();
      int numQueues = buffer.readInt();
      queueNames = new ArrayList<SimpleString>(numQueues);
      for (int i = 0; i < numQueues; i++)
      {
         queueNames.add(buffer.readSimpleString());
      }
   }

   public int getRequiredBufferSize()
   {
      int size = BASIC_PACKET_SIZE + DataConstants.SIZE_BOOLEAN + DataConstants.SIZE_INT;
      for (SimpleString queueName : queueNames)
      {
         size += queueName.sizeof();
      }
      return size;
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof SessionBindingQueryResponseMessage == false)
      {
         return false;
      }

      SessionBindingQueryResponseMessage r = (SessionBindingQueryResponseMessage)other;

      if (super.equals(other) && exists == r.exists)
      {
         if (queueNames.size() == r.queueNames.size())
         {
            for (int i = 0; i < queueNames.size(); i++)
            {
               if (!queueNames.get(i).equals(r.queueNames.get(i)))
               {
                  return false;
               }
            }
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }

      return true;
   }

}