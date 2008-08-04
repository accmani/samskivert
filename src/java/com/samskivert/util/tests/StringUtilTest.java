//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util.tests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.StringUtil;

public class StringUtilTest extends TestCase
{
    public StringUtilTest ()
    {
        super(StringUtilTest.class.getName());
    }

    public void runTest ()
    {
        String source = "mary, had, a,, little, lamb, and, a, comma,,";

        // split the source string into tokens
        String[] tokens = StringUtil.parseStringArray(source);
        assertTrue("tokens.length == 7", tokens.length == 7);

        // now join them back together
        String joined = StringUtil.joinEscaped(tokens);
        assertTrue("joined.equals(source)", joined.equals(source));

        // make sure null to empty string works
        tokens = new String[] { "this", null, "is", null, "a", null, "test" };
        joined = StringUtil.joinEscaped(tokens);
        assertTrue("null elements work", joined.equals("this, , is, , a, , test"));
    }

    public static Test suite ()
    {
        return new StringUtilTest();
    }
}
