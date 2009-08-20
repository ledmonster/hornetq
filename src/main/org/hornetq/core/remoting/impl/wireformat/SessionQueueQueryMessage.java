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

import org.hornetq.core.remoting.spi.MessagingBuffer;
import org.hornetq.utils.SimpleString;


/**
 * 
 * A SessionQueueQueryMessage
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class SessionQueueQueryMessage extends PacketImpl
{
   private SimpleString queueName;

   public SessionQueueQueryMessage(final SimpleString queueName)
   {
      super(SESS_QUEUEQUERY);

      this.queueName = queueName;            
   }
   
   public SessionQueueQueryMessage()
   {
      super(SESS_QUEUEQUERY);        
   }

   public SimpleString getQueueName()
   {
      return queueName;
   }

   public int getRequiredBufferSize()
   {
      return BASIC_PACKET_SIZE + queueName.sizeof();
   }


   public void encodeBody(final MessagingBuffer buffer)
   {
      buffer.writeSimpleString(queueName);
   }
   
   public void decodeBody(final MessagingBuffer buffer)
   {
      queueName = buffer.readSimpleString();
   }
   
   public boolean equals(Object other)
   {
      if (other instanceof SessionQueueQueryMessage == false)
      {
         return false;
      }
            
      SessionQueueQueryMessage r = (SessionQueueQueryMessage)other;
      
      return super.equals(other) && r.queueName.equals(this.queueName);
   }
   
}