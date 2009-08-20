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
import org.hornetq.utils.DataConstants;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @version <tt>$Revision$</tt>
 */
public class SessionAcknowledgeMessage extends PacketImpl
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private long consumerID;

   private long messageID;

   private boolean requiresResponse;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionAcknowledgeMessage(final long consumerID, final long messageID, final boolean requiresResponse)
   {
      super(SESS_ACKNOWLEDGE);

      this.consumerID = consumerID;

      this.messageID = messageID;

      this.requiresResponse = requiresResponse;
   }

   public SessionAcknowledgeMessage()
   {
      super(SESS_ACKNOWLEDGE);
   }

   // Public --------------------------------------------------------

   public long getConsumerID()
   {
      return consumerID;
   }

   public long getMessageID()
   {
      return messageID;
   }

   public boolean isRequiresResponse()
   {
      return requiresResponse;
   }

   public int getRequiredBufferSize()
   {
      return BASIC_PACKET_SIZE + DataConstants.SIZE_LONG + DataConstants.SIZE_LONG + DataConstants.SIZE_BOOLEAN;
   }

   @Override
   public void encodeBody(final MessagingBuffer buffer)
   {
      buffer.writeLong(consumerID);

      buffer.writeLong(messageID);

      buffer.writeBoolean(requiresResponse);
   }

   @Override
   public void decodeBody(final MessagingBuffer buffer)
   {
      consumerID = buffer.readLong();

      messageID = buffer.readLong();

      requiresResponse = buffer.readBoolean();
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof SessionAcknowledgeMessage == false)
      {
         return false;
      }

      SessionAcknowledgeMessage r = (SessionAcknowledgeMessage)other;

      return super.equals(other) && consumerID == r.consumerID &&
             messageID == r.messageID &&
             requiresResponse == r.requiresResponse;
   }
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}