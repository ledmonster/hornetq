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

package org.hornetq.tests.unit.jms.client;

import org.hornetq.jms.client.SelectorTranslator;
import org.hornetq.tests.util.UnitTestCase;

/**
 * 
 * A SelectorTranslatorTest
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class SelectorTranslatorTest extends UnitTestCase
{
   public void testParseNull()
   {
      assertNull(SelectorTranslator.convertToJBMFilterString(null));
   }
   
   public void testParseSimple()
   {
      final String selector = "color = 'red'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
   }
   
   public void testParseMoreComplex()
   {
      final String selector = "color = 'red' OR cheese = 'stilton' OR (age = 3 AND shoesize = 12)";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
   }
   
   public void testParseJMSDeliveryMode()
   {
      String selector = "JMSDeliveryMode='NON_PERSISTENT'";
      
      assertEquals("JBMDurable='NON_DURABLE'", SelectorTranslator.convertToJBMFilterString(selector));
            
      selector = "JMSDeliveryMode='PERSISTENT'";
      
      assertEquals("JBMDurable='DURABLE'", SelectorTranslator.convertToJBMFilterString(selector));
            
      selector = "color = 'red' AND 'NON_PERSISTENT' = JMSDeliveryMode";
      
      assertEquals("color = 'red' AND 'NON_DURABLE' = JBMDurable", SelectorTranslator.convertToJBMFilterString(selector));
            
      selector = "color = 'red' AND 'PERSISTENT' = JMSDeliveryMode";
      
      assertEquals("color = 'red' AND 'DURABLE' = JBMDurable", SelectorTranslator.convertToJBMFilterString(selector));
                  
      checkNoSubstitute("JMSDeliveryMode");     
   }
   
   public void testParseJMSPriority()
   {
      String selector = "JMSPriority=5";
      
      assertEquals("JBMPriority=5", SelectorTranslator.convertToJBMFilterString(selector));
            
      selector = " JMSPriority = 7";
      
      assertEquals(" JBMPriority = 7", SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSPriority = 7 OR 1 = JMSPriority AND (JMSPriority= 1 + 4)";
      
      assertEquals(" JBMPriority = 7 OR 1 = JBMPriority AND (JBMPriority= 1 + 4)", SelectorTranslator.convertToJBMFilterString(selector));
                        
      checkNoSubstitute("JMSPriority");      
      
      selector = "animal = 'lion' JMSPriority = 321 OR animal_name = 'xyzJMSPriorityxyz'";
      
      assertEquals("animal = 'lion' JBMPriority = 321 OR animal_name = 'xyzJMSPriorityxyz'", SelectorTranslator.convertToJBMFilterString(selector));
     
   }
   
   public void testParseJMSMessageID()
   {
      String selector = "JMSMessageID='ID:JBM-12435678";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSMessageID='ID:JBM-12435678";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSMessageID = 'ID:JBM-12435678";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " myHeader = JMSMessageID";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " myHeader = JMSMessageID OR (JMSMessageID = 'ID-JBM' + '12345')";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      checkNoSubstitute("JMSMessageID"); 
   }
   
   public void testParseJMSTimestamp()
   {
      String selector = "JMSTimestamp=12345678";
      
      assertEquals("JBMTimestamp=12345678", SelectorTranslator.convertToJBMFilterString(selector));
            
      selector = " JMSTimestamp=12345678";
      
      assertEquals(" JBMTimestamp=12345678", SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSTimestamp=12345678 OR 78766 = JMSTimestamp AND (JMSTimestamp= 1 + 4878787)";
      
      assertEquals(" JBMTimestamp=12345678 OR 78766 = JBMTimestamp AND (JBMTimestamp= 1 + 4878787)", SelectorTranslator.convertToJBMFilterString(selector));
      
                  
      checkNoSubstitute("JMSTimestamp"); 
      
      selector = "animal = 'lion' JMSTimestamp = 321 OR animal_name = 'xyzJMSTimestampxyz'";
      
      assertEquals("animal = 'lion' JBMTimestamp = 321 OR animal_name = 'xyzJMSTimestampxyz'", SelectorTranslator.convertToJBMFilterString(selector));
     
   }
   
   public void testParseJMSCorrelationID()
   {
      String selector = "JMSCorrelationID='ID:JBM-12435678";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSCorrelationID='ID:JBM-12435678";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSCorrelationID = 'ID:JBM-12435678";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " myHeader = JMSCorrelationID";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " myHeader = JMSCorrelationID OR (JMSCorrelationID = 'ID-JBM' + '12345')";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      checkNoSubstitute("JMSCorrelationID"); 
   }
   
   public void testParseJMSType()
   {
      String selector = "JMSType='aardvark'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSType='aardvark'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " JMSType = 'aardvark'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " myHeader = JMSType";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = " myHeader = JMSType OR (JMSType = 'aardvark' + 'sandwich')";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      checkNoSubstitute("JMSType"); 
   }
   
   // Private -------------------------------------------------------------------------------------
   
   private void checkNoSubstitute(String fieldName)
   {
      String selector = "Other" + fieldName + " = 767868";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "cheese = 'cheddar' AND Wrong" + fieldName +" = 54";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "fruit = 'pomegranate' AND " + fieldName + "NotThisOne = 'tuesday'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "animal = 'lion' AND animal_name = '" + fieldName + "'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "animal = 'lion' AND animal_name = ' " + fieldName + "'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "animal = 'lion' AND animal_name = ' " + fieldName + " '";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "animal = 'lion' AND animal_name = 'xyz " + fieldName +"'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "animal = 'lion' AND animal_name = 'xyz" + fieldName + "'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "animal = 'lion' AND animal_name = '" + fieldName + "xyz'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
      
      selector = "animal = 'lion' AND animal_name = 'xyz" + fieldName + "xyz'";
      
      assertEquals(selector, SelectorTranslator.convertToJBMFilterString(selector));
   }
   
}