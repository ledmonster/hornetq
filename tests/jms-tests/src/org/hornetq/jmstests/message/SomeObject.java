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

package org.hornetq.jmstests.message;

import java.io.Serializable;

/**
 * ObjectMessageTest needed a simple class to test ClassLoadingIsolations
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * @version <tt>$Revision$</tt>
 *
 *  $Id$
 */
public class SomeObject implements Serializable
{

   // Constants ------------------------------------------------------------------------------------

   // Attributes -----------------------------------------------------------------------------------

	private static final long serialVersionUID = -2939720794544432834L;
	
	int i;
   int j;

   // Static ---------------------------------------------------------------------------------------

   // Constructors ---------------------------------------------------------------------------------

   public SomeObject(int i, int j)
   {
      this.i=i;
      this.j=j;
   }

   // Public ---------------------------------------------------------------------------------------

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SomeObject that = (SomeObject) o;

      if (i != that.i) return false;
      if (j != that.j) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = i;
      result = 31 * result + j;
      return result;
   }

   // Package protected ----------------------------------------------------------------------------

   // Protected ------------------------------------------------------------------------------------

   // Private --------------------------------------------------------------------------------------

   // Inner classes --------------------------------------------------------------------------------

}