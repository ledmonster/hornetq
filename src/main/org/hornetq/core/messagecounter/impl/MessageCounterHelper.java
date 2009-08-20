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

package org.hornetq.core.messagecounter.impl;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import org.hornetq.core.management.DayCounterInfo;
import org.hornetq.core.messagecounter.MessageCounter;
import org.hornetq.core.messagecounter.MessageCounter.DayCounter;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 *
 * @version <tt>$Revision$</tt>
 *
 */
public class MessageCounterHelper
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static String listMessageCounterHistory(final MessageCounter counter) throws Exception
   {
      List<DayCounter> history = counter.getHistory();
      DayCounterInfo[] infos = new DayCounterInfo[history.size()];
      for (int i = 0; i < infos.length; i++)
      {
         DayCounter dayCounter = history.get(i);
         int[] counters = dayCounter.getCounters();
         GregorianCalendar date = dayCounter.getDate();

         DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
         String strData = dateFormat.format(date.getTime());
         infos[i] = new DayCounterInfo(strData, counters);
      }
      return DayCounterInfo.toJSON(infos);
   }
   
   public static String listMessageCounterAsHTML(MessageCounter[] counters)
   {
      if (counters == null)
         return null;

      String ret = "<table class=\"jbm-message-counter\">\n" 
                   + "<tr>"
                   + "<th>Type</th>"
                   + "<th>Name</th>"
                   + "<th>Subscription</th>"
                   + "<th>Durable</th>"
                   + "<th>Count</th>"
                   + "<th>CountDelta</th>"
                   + "<th>Depth</th>"
                   + "<th>DepthDelta</th>"
                   + "<th>Last Add</th>"
                   + "<th>Last Update</th>"
                   + "</tr>\n";

      for (int i = 0; i < counters.length; i++)
      {
         MessageCounter counter = counters[i];
         String type = counter.isDestinationTopic() ? "Topic" : "Queue";
         String subscription = counter.getDestinationSubscription();
         if (subscription == null)
         {
            subscription = "-";
         }
         String durableStr = "-"; // makes no sense for a queue
         if (counter.isDestinationTopic())
         {
            durableStr = Boolean.toString(counter.isDestinationDurable());            
         }
         ret += "<tr bgcolor=\"#" + ((i % 2) == 0 ? "FFFFFF" : "F0F0F0") + "\">";

         ret += "<td>" + type + "</td>";
         ret += "<td>" + counter.getDestinationName() + "</td>";
         ret += "<td>" + subscription + "</td>";
         ret += "<td>" + durableStr + "</td>";
         ret += "<td>" + counter.getCount() + "</td>";
         ret += "<td>" + prettify(counter.getCountDelta()) + "</td>";
         ret += "<td>" + prettify(counter.getMessageCount()) + "</td>";
         ret += "<td>" + prettify(counter.getMessageCountDelta()) + "</td>";
         ret += "<td>" + asDate(counter.getLastAddedMessageTime()) + "</td>";
         ret += "<td>" + asDate(counter.getLastUpdate()) + "</td>";
         
         ret += "</tr>\n";
      }

      ret += "</table>\n";

      return ret;
   }

   public static String listMessageCounterHistoryAsHTML(MessageCounter[] counters)
   {
      if (counters == null)
         return null;

      String ret = "<ul>\n";

      for (int i = 0; i < counters.length; i++)
      {
         ret += "<li>\n";
         ret += "  <ul>\n";

         ret += "    <li>";
         // destination name
         ret += (counters[i].isDestinationTopic() ? "Topic '" : "Queue '") + counters[i].getDestinationName() + "'";
         ret += "</li>\n";

         if (counters[i].getDestinationSubscription() != null)
         {
            ret += "    <li>";
            ret += "Subscription '" + counters[i].getDestinationSubscription() + "'";
            ret += "</li>\n";
         }

         ret += "    <li>";
         // table header
         ret += "<table class=\"jbm-message-counter-history\">\n";
         ret += "<tr><th>Date</th>";

         for (int j = 0; j < 24; j++)
            ret += "<th>" + j + "</th>";

         ret += "<th>Total</th></tr>\n";

         // get history data as CSV string
         StringTokenizer tokens = new StringTokenizer(counters[i].getHistoryAsString(), ",\n");

         // get history day count
         int days = Integer.parseInt(tokens.nextToken());

         for (int j = 0; j < days; j++)
         {
            // next day counter row
            ret += "<tr bgcolor=\"#" + ((j % 2) == 0 ? "FFFFFF" : "F0F0F0") + "\">";

            // date
            ret += "<td>" + tokens.nextToken() + "</td>";

            // 24 hour counters
            int total = 0;

            for (int k = 0; k < 24; k++)
            {
               int value = Integer.parseInt(tokens.nextToken().trim());

               if (value == -1)
               {
                  ret += "<td></td>";
               }
               else
               {
                  ret += "<td>" + value + "</td>";

                  total += value;
               }
            }

            ret += "<td>" + total + "</td></tr>\n";
         }

         ret += "</table></li>\n";
         ret += "  </ul>\n";
         ret += "</li>\n";
      }

      ret += "</ul>\n";

      return ret;
   }
   
   private static String prettify(long value)
   {
      if (value == 0)
      {
         return "-";
      }
      return Long.toString(value);
   }
   
   private static String asDate(long time)
   {
      if (time > 0)
      {
         return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date(time));
      }
      else
      {
         return "-";
      }
   }

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}